package de.jerleo.android.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.jerleo.android.R
import de.jerleo.android.activity.ActivityMain
import de.jerleo.database.Constants
import de.jerleo.model.Bill
import java.text.DecimalFormat

internal class AdapterBillHistory(
    private var ctx: Context, textView: Int,
    private var bill: Bill,
    years: List<Int>
) : ArrayAdapter<Int>(ctx, textView, years) {

    private val currencyFormat: String = ActivityMain.currencyFormatShort
    private var decimalFormat: DecimalFormat = ActivityMain.decimalFormat
    private var values: MutableList<HashMap<String, String>> = ArrayList()

    init {
        years.forEach {
            val billFees = bill.fees(it)
            val billCosts = bill.costs(it)
            val billPayments = bill.payments(it)
            val billBalance = billPayments - billFees - billCosts
            val billUsage = bill.usage(it)

            val entry: HashMap<String, String> = HashMap()
            entry[Constants.YEAR] = it.toString()
            entry[Constants.USAGE] = decimalFormat.format(billUsage)
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
            row = inflater.inflate(R.layout.bill_history_row, parent, false)

            val holder = ViewHolder().apply {
                yearView = row.findViewById(R.id.bill_year)
                usageView = row.findViewById(R.id.bill_usage)
                feesView = row.findViewById(R.id.bill_fees)
                costsView = row.findViewById(R.id.bill_costs)
                paymentsView = row.findViewById(R.id.bill_payments)
                balanceView = row.findViewById(R.id.bill_balance)
                textColor = balanceView.currentTextColor
            }
            row.tag = holder
        }

        val holder = row!!.tag as ViewHolder
        holder.apply {
            yearView.text = values[position][Constants.YEAR]
            usageView.text = values[position][Constants.USAGE]
            feesView.text = values[position][Constants.FEE]
            costsView.text = values[position][Constants.COSTS]
            paymentsView.text = values[position][Constants.PAYMENT]
            balanceView.text = values[position][Constants.BALANCE]
            if (balanceView.text.contains("-"))
                balanceView.setTextColor(Color.RED)
            else
                balanceView.setTextColor(holder.textColor)
        }
        return row
    }

    internal class ViewHolder {
        lateinit var yearView: TextView
        lateinit var usageView: TextView
        lateinit var feesView: TextView
        lateinit var costsView: TextView
        lateinit var paymentsView: TextView
        lateinit var balanceView: TextView
        var textColor = 0
    }
}