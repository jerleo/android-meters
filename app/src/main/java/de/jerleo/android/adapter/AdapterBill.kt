package de.jerleo.android.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.jerleo.android.DateHelper
import de.jerleo.android.DialogHelper
import de.jerleo.android.R
import de.jerleo.android.activity.ActivityMain
import de.jerleo.database.Constants
import de.jerleo.model.Bill

internal class AdapterBill(
    private var ctx: Context, textView: Int,
    private var bills: List<Bill>
) : ArrayAdapter<Bill>(ctx, textView, bills), DialogHelper.OnListChangedListener {

    private val currencyFormat: String = ActivityMain.currencyFormat
    private var values: MutableList<HashMap<String, String>> = ArrayList()

    init {
        fillValues()
    }

    override fun onListChanged() {
        fillValues()
    }

    private fun fillValues() {
        values.clear()
        bills.forEach {
            val year = it.dateFrom.year
            val billPayments = it.payments(year)
            val billFees = it.fees(year)
            val billCosts = it.costs(year)
            val billBalance = billPayments - billFees - billCosts

            val entry: HashMap<String, String> = HashMap()
            entry[Constants.DESCRIPTION] = it.name
            entry[Constants.BEGIN] = DateHelper.formatMedium(it.dateFrom)
            entry[Constants.END] = DateHelper.formatMedium(it.dateTo)
            entry[Constants.FEE] = String.format(currencyFormat, -billFees)
            entry[Constants.COSTS] = String.format(currencyFormat, -billCosts)
            entry[Constants.PAYMENT] = String.format(currencyFormat, billPayments)
            entry[Constants.BALANCE] = String.format(currencyFormat, billBalance)
            values.add(entry)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        if (row == null) {
            val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            row = inflater.inflate(R.layout.bill_row, parent, false)

            val holder = ViewHolder().apply {
                description = row.findViewById(R.id.description)
                begin = row.findViewById(R.id.begin)
                end = row.findViewById(R.id.end)
                fees = row.findViewById(R.id.fees)
                costs = row.findViewById(R.id.costs)
                payments = row.findViewById(R.id.payments)
                balance = row.findViewById(R.id.balance)
                textColor = balance.currentTextColor
            }
            row.tag = holder
        }

        val holder = row!!.tag as ViewHolder
        holder.apply {
            description.text = values[position][Constants.DESCRIPTION]
            begin.text = values[position][Constants.BEGIN]
            end.text = values[position][Constants.END]
            fees.text = values[position][Constants.FEE]
            costs.text = values[position][Constants.COSTS]
            payments.text = values[position][Constants.PAYMENT]
            balance.text = values[position][Constants.BALANCE]
            if (balance.text.contains("-"))
                balance.setTextColor(Color.RED)
            else
                balance.setTextColor(holder.textColor)
        }
        return row
    }

    internal class ViewHolder {
        lateinit var description: TextView
        lateinit var begin: TextView
        lateinit var end: TextView
        lateinit var fees: TextView
        lateinit var costs: TextView
        lateinit var payments: TextView
        lateinit var balance: TextView
        var textColor = 0
    }
}