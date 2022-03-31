@file:Suppress("DEPRECATION")

package de.jerleo.android.list

import android.app.ListActivity
import android.os.Bundle
import de.jerleo.android.R
import de.jerleo.android.adapter.AdapterBillHistory
import de.jerleo.database.Constants
import de.jerleo.model.Bill
import de.jerleo.model.Home

class ListBillHistory : ListActivity() {

    private lateinit var bill: Bill
    private lateinit var adapterBillHistory: AdapterBillHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bill_history_list)
        registerForContextMenu(listView)

        val bundle = this.intent.extras
        val position = bundle!!.getInt(Constants.BILL)
        bill = Home.instance.bill(position)
        title = getString(R.string.history) + ": " + bill.name

        adapterBillHistory = AdapterBillHistory(this, R.id.bill_row_history, bill, bill.years())
        listAdapter = adapterBillHistory
    }
}