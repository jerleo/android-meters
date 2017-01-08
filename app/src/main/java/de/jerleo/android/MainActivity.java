package de.jerleo.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Currency;
import java.util.Locale;

import de.jerleo.database.Database;
import de.jerleo.model.Home;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MainActivity extends AppCompatActivity implements MeterList.OnMeterChangedListener {

    private static final DialogHelper dialogHelper = new DialogHelper();
    private static final Currency currency = Currency.getInstance(Locale.getDefault());

    private static Home home;
    private static Database database;

    private static MeterList meterList;
    private static BillList billList;

    private static File externalDir;

    private ViewPager mViewPager;

    public static String getCurrencyFormat() {

        return "%." + currency.getDefaultFractionDigits() + "f " + currency.getSymbol();
    }

    public static String getCurrencySymbol() {

        return currency.getSymbol();
    }

    public static Home getHome() {

        return home;
    }

    @Override
    public void finish() {

        super.finish();
        database.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            onMeterChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_main, menu);

        MenuItem itemImport = menu.findItem(R.id.action_import);
        MenuItem itemExport = menu.findItem(R.id.action_export);

        itemImport.setEnabled(isExternalStorageWritable());
        itemExport.setEnabled(isExternalStorageWritable());

        invalidateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onMeterChanged() {

        meterList.notifyDataSetChanged();
        billList.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {

            case R.id.action_new_meter:
                mViewPager.setCurrentItem(0);
                meterList.create();
                break;

            case R.id.action_new_bill:
                mViewPager.setCurrentItem(1);
                billList.create();
                break;

            case R.id.action_import:
                importData();
                break;

            case R.id.action_export:
                exportData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportData() {

        try {
            home.exportTo(externalDir);
            toast(R.string.export_success);
        } catch (IOException e) {
            toast(R.string.export_failure);
        }
    }

    private void importData() {

        final AlertDialog dialog = dialogHelper.getChoiceDialog(this, R.string.import_title, externalDir.list());
        dialog.show();
        final Button ok = dialog.getButton(BUTTON_POSITIVE);
        ok.setEnabled(false);
        ok.setTag(new ImportPrepare());
    }

    private boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void toast(int resId) {

        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final static int NUMBER_OF_TABS = 2;

        public SectionsPagerAdapter(FragmentManager fm) {

            super(fm);
        }

        @Override
        public int getCount() {

            return NUMBER_OF_TABS;
        }

        @Override
        public Fragment getItem(int position) {

            return position == 0 ? meterList : billList;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            String[] titles = {getString(R.string.meters), getString(R.string.bills)};
            return titles[position];
        }
    }

    private class ImportExecute implements DialogHelper.DialogCommand {

        private final Home importedHome;

        public ImportExecute(Home home) {

            importedHome = home;
        }

        @Override
        public void execute() {

            final Toast completed = Toast.makeText(MainActivity.this, R.string.import_success,
                    Toast.LENGTH_SHORT);

            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
            progress.setIndeterminate(true);
            progress.setProgress(0);
            progress.show();

            @SuppressLint("HandlerLeak")
            final Handler handler = new Handler() {

                @Override
                public void handleMessage(Message msg) {

                    super.handleMessage(msg);

                    MeterAdapter meterAdapter = (MeterAdapter) meterList.getListAdapter();
                    meterAdapter.clear();
                    meterAdapter.addAll(importedHome.getMeters());

                    BillAdapter billAdapter = (BillAdapter) billList.getListAdapter();
                    billAdapter.clear();
                    billAdapter.addAll(importedHome.getBills());

                    progress.dismiss();
                    completed.show();
                    setResult(Activity.RESULT_OK);
                }
            };

            new Thread(new Runnable() {

                @Override
                public void run() {

                    progress.setMessage(getString(R.string.importing));

                    database.rebuild(null);
                    home.replaceWith(importedHome);

                    handler.sendMessage(handler.obtainMessage());

                }
            }).start();
        }
    }

    private class ImportPrepare implements DialogHelper.DialogCommand {

        private Home newHome;

        @Override
        public void execute() {

            final String path = externalDir.getAbsolutePath();
            final String file = dialogHelper.getChoice();
            try {
                newHome = new Home(new File(path, file));
                boolean success = newHome.getMeters().size() > 0;
                if (success)
                    confirm();
                else
                    toast(R.string.missing_entries);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void confirm() {

            final AlertDialog dialog = dialogHelper.getAlertDialog(MainActivity.this,
                    R.string.import_title, R.string.confirm_import);
            dialog.show();
            dialog.getButton(BUTTON_POSITIVE).setTag(new ImportExecute(newHome));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        DateHelper.setContext(this);

        database = Database.getInstance(this);

        if (home == null)
            home = new Home();

        externalDir = getExternalFilesDir(null);

        if (meterList == null)
            meterList = new MeterList();
        meterList.setRetainInstance(true);

        if (billList == null)
            billList = new BillList();
        billList.setRetainInstance(true);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        finish();
    }
}
