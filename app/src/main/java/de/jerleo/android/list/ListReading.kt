@file:Suppress("DEPRECATION")

package de.jerleo.android.list

import android.app.ListActivity
import android.content.DialogInterface
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import de.jerleo.android.DialogHelper
import de.jerleo.android.DialogHelper.DialogCommand
import de.jerleo.android.R
import de.jerleo.android.adapter.AdapterReading
import de.jerleo.database.Constants
import de.jerleo.model.Home
import de.jerleo.model.Meter

class ListReading : ListActivity() {

    private val dialogHelper = DialogHelper()
    private lateinit var meter: Meter
    private lateinit var adapterReading: AdapterReading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reading_list)
        registerForContextMenu(listView)

        val bundle = this.intent.extras
        bundle?.let {
            val meterId = it.getLong(Constants.METER)
            val meter = Home.instance.meter(meterId)
            meter?.let {
                this.meter = it
                title = getString(R.string.readings) + ": " + meter.number
                adapterReading = AdapterReading(this, R.id.reading_row, meter.readings)
                listAdapter = adapterReading
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_reading_list, menu)
        val info = menuInfo as AdapterContextMenuInfo
        if (info.position != 0) {
            val item = menu.findItem(R.id.delete)
            item.isEnabled = false
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete) {
            delete()
        }
        return super.onContextItemSelected(item)
    }

    private fun delete() {
        val dialog = dialogHelper.getAlertDialog(
            this, R.string.reading,
            R.string.delete_message
        )
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).tag = DeleteCommand()
    }

    private inner class DeleteCommand : DialogCommand {
        override fun execute() {
            meter.deleteLastReading()
            adapterReading.notifyDataSetChanged()
            setResult(RESULT_OK)
        }
    }
}