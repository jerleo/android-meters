package de.jerleo.android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import java.util.List;

import de.jerleo.model.Meter;
import de.jerleo.model.Tariff;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class TariffList extends ListActivity {

    private final DialogHelper dialogHelper = new DialogHelper();

    private Meter meter;
    private TariffAdapter adapter;

    private int meterPosition;
    private int deletePosition;

    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();
        if (adapter.isEmpty())
            openOptionsMenu();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.change:
                change(info.position);
                break;
            case R.id.delete:
                delete(info.position);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_tariff_list, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_tariff_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new_tariff:
                create();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void change(int position) {

        final Intent intent = new Intent(TariffList.this, TariffActivity.class);
        intent.putExtra("meter", meterPosition);
        intent.putExtra("tariff", position);
        startActivityForResult(intent, RequestCode.TARIFF_CHANGE);
    }

    private void create() {

        final Intent intent = new Intent(TariffList.this, TariffActivity.class);
        intent.putExtra("meter", meterPosition);
        startActivityForResult(intent, RequestCode.TARIFF_CREATE);
    }

    private void delete(int position) {

        deletePosition = position;
        final AlertDialog dialog = dialogHelper.getAlertDialog(this,
                R.string.delete, R.string.delete_message);
        dialog.show();
        dialog.getButton(BUTTON_POSITIVE).setTag(new DeleteCommand());
    }

    private class DeleteCommand implements DialogHelper.DialogCommand {

        @Override
        public void execute() {

            final Tariff tariff = adapter.getItem(deletePosition);
            meter.delete(tariff);
            adapter.notifyDataSetChanged();
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            adapter.notifyDataSetChanged();
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        final ListView listView = getListView();
        registerForContextMenu(listView);

        final Bundle bundle = this.getIntent().getExtras();
        meterPosition = bundle.getInt("meter");
        meter = MainActivity.getHome().getMeter(meterPosition);
        List<Tariff> tariffs = meter.getTariffs();

        adapter = new TariffAdapter(this, R.id.tariff_row, tariffs);
        setListAdapter(adapter);
        setTitle(getString(R.string.tariffs) + ": " + meter.getName());
    }
}