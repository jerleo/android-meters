package de.jerleo.android.list

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.ListFragment
import de.jerleo.android.DialogHelper
import de.jerleo.android.DialogHelper.DialogCommand
import de.jerleo.android.R
import de.jerleo.android.activity.ActivityBill
import de.jerleo.android.adapter.AdapterBill
import de.jerleo.database.Constants
import de.jerleo.model.Home

class ListBill : ListFragment() {

    private var deletePosition = 0

    private val activity: Activity by lazy { requireActivity() }
    private val callback by lazy { context as DialogHelper.OnListChangedListener }
    private val dialogHelper by lazy { DialogHelper() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(listView)
        listAdapter = AdapterBill(activity, R.id.bill_row, Home.instance.bills)
    }

    override fun onResume() {
        super.onResume()
        listView.requestFocus() // restore focus after swiping
        callback.onListChanged() // update lists
        activity.closeContextMenu()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (isResumed) { // instead of userVisibleHint
            val info = item.menuInfo as AdapterContextMenuInfo
            when (item.itemId) {
                R.id.change -> change(info.position)
                R.id.delete -> delete(info.position)
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = activity.menuInflater
        inflater.inflate(R.menu.context_bill_list, menu)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        val intent = Intent(activity, ListBillHistory::class.java)
        intent.putExtra(Constants.BILL, position)
        startActivity(intent)
    }

    private val billChange =
        registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK)
                callback.onListChanged()
        }

    private fun change(position: Int) {
        val intent = Intent(activity, ActivityBill::class.java)
        intent.putExtra(Constants.BILL, position)
        billChange.launch(intent)
    }

    private fun delete(position: Int) {
        deletePosition = position
        val dialog = dialogHelper.getAlertDialog(activity, R.string.bill, R.string.delete_message)
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).tag = DeleteCommand()
    }

    private inner class DeleteCommand : DialogCommand {
        override fun execute() {
            val adapter = listAdapter as AdapterBill
            val bill = adapter.getItem(deletePosition)
            Home.instance.remove(bill!!)
            adapter.notifyDataSetChanged()
            activity.setResult(Activity.RESULT_OK)
        }
    }
}