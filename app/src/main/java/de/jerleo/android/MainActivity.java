package de.jerleo.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Currency;
import java.util.Locale;

import de.jerleo.database.Database;
import de.jerleo.model.Home;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MainActivity extends AppCompatActivity implements MeterList.OnMeterChangedListener {

    private static final DialogHelper dialogHelper = new DialogHelper();

    private static final int IMPORT_REQUEST = 23;
    private static final int EXPORT_REQUEST = 42;

    private static Home home;
    private static Database database;

    private MeterList meterList;
    private BillList billList;
    private ViewPager mViewPager;

    private static Currency getCurrency() {

        return Currency.getInstance(Locale.getDefault());
    }

    public static String getCurrencyFormat() {

        return "%." + getCurrency().getDefaultFractionDigits() + "f " + getCurrency().getSymbol();
    }

    public static String getCurrencySymbol() {

        return getCurrency().getSymbol();
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

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {

            case EXPORT_REQUEST:
                Uri uri = data.getData();
                new Exporter(uri).execute();
                return;

            case IMPORT_REQUEST:
                uri = data.getData();
                new Importer().prepare(uri);
                return;
        }
        onMeterChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_main, menu);
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

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/json");

        final String dateTime = DateHelper.getTimestamp(DateHelper.getNow());
        final String filename = String.format("Export_%s.txt", dateTime);

        intent.putExtra(Intent.EXTRA_TITLE, filename);
        startActivityForResult(intent, EXPORT_REQUEST);
    }

    private void importData() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, IMPORT_REQUEST);
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

        if (meterList == null)
            meterList = new MeterList();
        meterList.setRetainInstance(true);

        if (billList == null)
            billList = new BillList();
        billList.setRetainInstance(true);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        finish();
    }

    private class Exporter implements DialogHelper.DialogCommand {

        private final Uri exportUri;

        Exporter(Uri uri) {
            this.exportUri = uri;
        }

        @Override
        public void execute() {
            final Toast toast = Toast.makeText(MainActivity.this, R.string.export_success,
                    Toast.LENGTH_SHORT);

            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
            progress.setIndeterminate(true);
            progress.setProgress(0);
            progress.show();

            @SuppressLint("HandlerLeak") final Handler handler = new Handler() {

                @Override
                public void handleMessage(Message msg) {

                    super.handleMessage(msg);
                    progress.dismiss();
                    toast.show();
                    setResult(Activity.RESULT_OK);
                }
            };

            new Thread(() -> {

                progress.setMessage(getString(R.string.exporting));

                String data = home.exportJSON();
                try {
                    Context context = MainActivity.this;
                    OutputStream stream = context.getContentResolver().openOutputStream(exportUri);
                    stream.write(data.getBytes());
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                handler.sendMessage(handler.obtainMessage());
            }).start();
        }
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

    private class Importer implements DialogHelper.DialogCommand {

        private Home newHome;

        @Override
        public void execute() {

            final Toast toast = Toast.makeText(MainActivity.this, R.string.import_success,
                    Toast.LENGTH_SHORT);

            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
            progress.setIndeterminate(true);
            progress.setProgress(0);
            progress.show();

            @SuppressLint("HandlerLeak") final Handler handler = new Handler() {

                @Override
                public void handleMessage(Message msg) {

                    super.handleMessage(msg);

                    MeterAdapter meterAdapter = (MeterAdapter) meterList.getListAdapter();
                    meterAdapter.clear();
                    meterAdapter.addAll(newHome.getMeters());

                    BillAdapter billAdapter = (BillAdapter) billList.getListAdapter();
                    billAdapter.clear();
                    billAdapter.addAll(newHome.getBills());

                    progress.dismiss();
                    toast.show();
                    setResult(Activity.RESULT_OK);
                }
            };

            new Thread(() -> {

                progress.setMessage(getString(R.string.importing));

                database.rebuild(null);
                home.replaceWith(newHome);

                handler.sendMessage(handler.obtainMessage());
            }).start();
        }

        public void prepare(Uri importUri) {

            try {
                Context context = MainActivity.this;
                InputStream stream = context.getContentResolver().openInputStream(importUri);
                newHome = new Home(stream);
                stream.close();

                if (newHome.getMeters().size() > 0)
                    confirm();
                else
                    Toast.makeText(context, R.string.missing_entries, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void confirm() {

            final AlertDialog dialog = dialogHelper.getAlertDialog(MainActivity.this,
                    R.string.import_title, R.string.confirm_import);
            dialog.show();
            dialog.getButton(BUTTON_POSITIVE).setTag(this);
        }
    }
}
