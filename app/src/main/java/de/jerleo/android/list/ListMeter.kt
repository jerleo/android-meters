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
import de.jerleo.android.activity.ActivityMain
import de.jerleo.android.activity.ActivityMeter
import de.jerleo.android.activity.ActivityReading
import de.jerleo.android.activity.ReadingChart
import de.jerleo.android.adapter.AdapterMeter
import de.jerleo.database.Constants
import de.jerleo.model.Home
import de.jerleo.model.Meter

class ListMeter : ListFragment() {

    private var deleteId = 0L

    private val activity: Activity by lazy { requireActivity() }
    private val callback by lazy { context as OnListChangedListener }
    private val dialogHelper by lazy { DialogHelper() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(listView)
        val meters = (activity as ActivityMain).metersFiltered()
        listAdapter = AdapterMeter(activity, R.id.meter_row, meters, this)
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
            listAdapter?.let {
                val meterId = (it.getItem(info.position) as Meter).id
                when (item.itemId) {
                    R.id.change -> change(meterId)
                    R.id.delete -> delete(meterId)
                    R.id.tariffs -> showTariffs(meterId)
                }
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
        val meterId = (l.adapter.getItem(position) as Meter).id
        val intent = Intent(activity, ActivityReading::class.java)
        intent.putExtra(Constants.METER, meterId)
        startActivity(intent)
    }

    fun showChart(meterId: Long) {
        val intent = Intent(activity, ReadingChart::class.java)
        intent.putExtra(Constants.METER, meterId)
        startActivity(intent)
    }

    fun showReadings(meterId: Long) {
        val intent = Intent(activity, ListReading::class.java)
        intent.putExtra(Constants.METER, meterId)
        startActivity(intent)
    }

    private fun showTariffs(meterId: Long) {
        val intent = Intent(activity, ListTariff::class.java)
        intent.putExtra(Constants.METER, meterId)
        startActivity(intent)
    }

    private fun change(meterId: Long) {
        val intent = Intent(activity, ActivityMeter::class.java)
        intent.putExtra(Constants.METER, meterId)
        startActivity(intent)
    }

    private fun delete(meterId: Long) {
        deleteId = meterId
        val dialog = dialogHelper.getAlertDialog(activity, R.string.meter, R.string.delete_message)
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).tag = DeleteCommand()
    }

    private inner class DeleteCommand : DialogCommand {
        override fun execute() {
            val meter = Home.instance.meter(deleteId)
            meter?.let {
                Home.instance.remove(meter)
                callback.onListChanged()
                activity.setResult(Activity.RESULT_OK)
            }
        }
    }
}