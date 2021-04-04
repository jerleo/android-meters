package de.jerleo.android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import java.util.List;

import de.jerleo.model.Meter;
import de.jerleo.model.Reading;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class ReadingList extends ListActivity {

    private final DialogHelper dialogHelper = new DialogHelper();

    private Meter meter;
    private ReadingAdapter adapter;

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.delete) {
            delete();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_reading_list, menu);

        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if (info.position != 0) {
            final MenuItem item = menu.findItem(R.id.delete);
            item.setEnabled(false);
        }
    }

    private void delete() {

        final AlertDialog dialog = dialogHelper.getAlertDialog(this,
                R.string.reading, R.string.delete_message);
        dialog.show();
        dialog.getButton(BUTTON_POSITIVE).setTag(new DeleteCommand());
    }

    private class DeleteCommand implements DialogHelper.DialogCommand {

        @Override
        public void execute() {

            meter.deleteLastReading();
            adapter.notifyDataSetChanged();
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        final Bundle bundle = this.getIntent().getExtras();

        int meterPosition = bundle.getInt("meter");
        meter = MainActivity.getHome().getMeter(meterPosition);
        List<Reading> readings = meter.getReadings();

        adapter = new ReadingAdapter(this, R.id.reading_row, readings);
        setListAdapter(adapter);

        setTitle(getString(R.string.readings) + ": " + meter.getNumber());
        registerForContextMenu(getListView());
    }
}