@file:Suppress("DEPRECATION")

package de.jerleo.android.list

import android.app.ListActivity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import de.jerleo.android.DialogHelper
import de.jerleo.android.DialogHelper.DialogCommand
import de.jerleo.android.R
import de.jerleo.android.RequestCode
import de.jerleo.android.activity.ActivityTariff
import de.jerleo.android.adapter.AdapterTariff
import de.jerleo.database.Constants
import de.jerleo.model.Home
import de.jerleo.model.Meter

class ListTariff : ListActivity() {

    private val dialogHelper = DialogHelper()
    private var meterPosition = 0
    private var deletePosition = 0

    private lateinit var meter: Meter
    private lateinit var adapterTariff: AdapterTariff

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tariff_list)
        registerForContextMenu(listView)

        val bundle = this.intent.extras
        meterPosition = bundle!!.getInt(Constants.METER)
        meter = Home.instance.meter(meterPosition)
        title = getString(R.string.tariffs) + ": " + meter.name

        adapterTariff = AdapterTariff(this, R.id.tariff_row, meter.tariffs)
        listAdapter = adapterTariff
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            adapterTariff.notifyDataSetChanged()
            setResult(RESULT_OK)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo)
        menuInflater.inflate(R.menu.context_tariff_list, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        when (item.itemId) {
            R.id.change -> change(info.position)
            R.id.delete -> delete(info.position)
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_tariff_list, menu)
        invalidateOptionsMenu()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_new_tariff) create()
        return super.onOptionsItemSelected(item)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (adapterTariff.isEmpty) openOptionsMenu()
    }

    private fun create() {
        val intent = Intent(this@ListTariff, ActivityTariff::class.java)
        intent.putExtra(Constants.METER, meterPosition)
        startActivityForResult(intent, RequestCode.TARIFF_CREATE)
    }

    private fun change(position: Int) {
        val intent = Intent(this@ListTariff, ActivityTariff::class.java)
        intent.putExtra(Constants.METER, meterPosition)
        intent.putExtra(Constants.TARIFF, position)
        startActivityForResult(intent, RequestCode.TARIFF_CHANGE)
    }

    private fun delete(position: Int) {
        deletePosition = position
        val dialog = dialogHelper.getAlertDialog(this, R.string.delete, R.string.delete_message)
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).tag = DeleteCommand()
    }

    private inner class DeleteCommand : DialogCommand {
        override fun execute() {
            val tariff = adapterTariff.getItem(deletePosition)
            meter.delete(tariff!!)
            adapterTariff.notifyDataSetChanged()
            setResult(RESULT_OK)
        }
    }
}