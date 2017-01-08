package de.jerleo.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import de.jerleo.model.Bill;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class BillList extends ListFragment {

    private final DialogHelper dialogHelper = new DialogHelper();

    private Activity activity;
    private BillAdapter adapter;
    private int deletePosition;

    public void create() {

        startActivityForResult(new Intent(activity, BillActivity.class), RequestCode.BILL_CREATE);
    }

    public void notifyDataSetChanged() {

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
        activity = getActivity();
        adapter = new BillAdapter(activity, MainActivity.getHome().getBills());
        setListAdapter(adapter);
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
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.context_bill_list, menu);
    }

    private void change(int position) {

        final Intent intent = new Intent(activity, BillActivity.class);
        intent.putExtra("bill", position);
        startActivityForResult(intent, RequestCode.BILL_CHANGE);
    }

    private void delete(int position) {

        deletePosition = position;
        final AlertDialog dialog = dialogHelper.getAlertDialog(
                activity, R.string.bill, R.string.delete_message);
        dialog.show();
        dialog.getButton(BUTTON_POSITIVE).setTag(new DeleteCommand());
    }

    private class DeleteCommand implements DialogHelper.DialogCommand {

        @Override
        public void execute() {

            final Bill bill = adapter.getItem(deletePosition);
            MainActivity.getHome().deleteBill(bill);
            adapter.notifyDataSetChanged();
            activity.setResult(Activity.RESULT_OK);
        }
    }
}
