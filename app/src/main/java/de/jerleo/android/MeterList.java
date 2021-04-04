package de.jerleo.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import java.util.List;

import de.jerleo.model.Meter;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MeterList extends ListFragment {

    private final DialogHelper dialogHelper = new DialogHelper();

    private OnMeterChangedListener callback;

    private Activity activity;
    private MeterAdapter adapter;
    private int deletePosition;

    public void create() {

        activity.startActivityForResult(new Intent(activity, MeterActivity.class),
                RequestCode.METER_CREATE);
    }

    public void notifyDataSetChanged() {

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
        activity = getActivity();
        List<Meter> meters = MainActivity.getHome().getMeters();
        adapter = new MeterAdapter(activity, R.id.meter_row, meters, this);
        setListAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        callback = (OnMeterChangedListener) context;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (getUserVisibleHint()) {

            final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
                case R.id.change:
                    change(info.position);
                    break;
                case R.id.delete:
                    delete(info.position);
                    break;
                case R.id.tariffs:
                    showTariffs(info.position);
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.context_meter_list, menu);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);
        final Intent intent = new Intent(activity, ReadingActivity.class);
        intent.putExtra("meter", position);
        startActivityForResult(intent, RequestCode.READING_CREATE);
    }

    public void showChart(int position) {

        final Intent intent = new Intent(activity, ReadingChart.class);
        intent.putExtra("meter", position);
        startActivityForResult(intent, RequestCode.READING_CHART);
    }

    public void showReadings(int position) {

        final Intent intent = new Intent(activity, ReadingList.class);
        intent.putExtra("meter", position);
        startActivityForResult(intent, RequestCode.READING_LIST);
    }

    private void change(int position) {

        final Intent intent = new Intent(activity, MeterActivity.class);
        intent.putExtra("meter", position);
        activity.startActivityForResult(intent, RequestCode.METER_CHANGE);
    }

    private void delete(int position) {

        deletePosition = position;
        final AlertDialog dialog = dialogHelper.getAlertDialog(activity,
                R.string.meter, R.string.delete_message);
        dialog.show();
        dialog.getButton(BUTTON_POSITIVE).setTag(new DeleteCommand());
    }

    private void showTariffs(int position) {

        final Intent intent = new Intent(activity, TariffList.class);
        intent.putExtra("meter", position);
        startActivityForResult(intent, RequestCode.TARIFF_LIST);
    }

    public interface OnMeterChangedListener {

        void onMeterChanged();
    }

    private class DeleteCommand implements DialogHelper.DialogCommand {

        @Override
        public void execute() {

            final Meter meter = adapter.getItem(deletePosition);
            MainActivity.getHome().deleteMeter(meter);
            callback.onMeterChanged();
            activity.setResult(Activity.RESULT_OK);
        }
    }
}