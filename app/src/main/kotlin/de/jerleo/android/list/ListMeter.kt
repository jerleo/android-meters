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
import androidx.fragment.app.ListFragment
import de.jerleo.android.DialogHelper
import de.jerleo.android.DialogHelper.DialogCommand
import de.jerleo.android.DialogHelper.OnListChangedListener
import de.jerleo.android.R
import de.jerleo.android.activity.ActivityMeter
import de.jerleo.android.activity.ActivityReading
import de.jerleo.android.activity.ReadingChart
import de.jerleo.android.adapter.AdapterMeter
import de.jerleo.database.Constants
import de.jerleo.model.Home

class ListMeter : ListFragment() {

    private var deletePosition = 0

    private val activity: Activity by lazy { requireActivity() }
    private val callback by lazy { context as OnListChangedListener }
    private val dialogHelper by lazy { DialogHelper() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(listView)
        listAdapter = AdapterMeter(activity, R.id.meter_row, Home.instance.meters, this)
    }

    override fun onResume() {
        super.onResume()
        listView.requestFocus()  // restore focus after swiping
        callback.onListChanged() // update lists
        activity.closeContextMenu()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (isResumed) { // instead of userVisibleHint
            val info = item.menuInfo as AdapterContextMenuInfo
            when (item.itemId) {
                R.id.change -> change(info.position)
                R.id.delete -> delete(info.position)
                R.id.tariffs -> showTariffs(info.position)
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity.menuInflater.inflate(R.menu.context_meter_list, menu)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        val intent = Intent(activity, ActivityReading::class.java)
        intent.putExtra(Constants.METER, position)
        startActivity(intent)
    }

    fun showChart(position: Int) {
        val intent = Intent(activity, ReadingChart::class.java)
        intent.putExtra(Constants.METER, position)
        startActivity(intent)
    }

    fun showReadings(position: Int) {
        val intent = Intent(activity, ListReading::class.java)
        intent.putExtra(Constants.METER, position)
        startActivity(intent)
    }

    private fun showTariffs(position: Int) {
        val intent = Intent(activity, ListTariff::class.java)
        intent.putExtra(Constants.METER, position)
        startActivity(intent)
    }

    private fun change(position: Int) {
        val intent = Intent(activity, ActivityMeter::class.java)
        intent.putExtra(Constants.METER, position)
        startActivity(intent)
    }

    private fun delete(position: Int) {
        deletePosition = position
        val dialog = dialogHelper.getAlertDialog(activity, R.string.meter, R.string.delete_message)
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).tag = DeleteCommand()
    }

    private inner class DeleteCommand : DialogCommand {
        override fun execute() {
            val adapter = listAdapter as AdapterMeter
            val meter = adapter.getItem(deletePosition)
            Home.instance.remove(meter!!)
            callback.onListChanged()
            activity.setResult(Activity.RESULT_OK)
        }
    }
}