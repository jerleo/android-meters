package de.jerleo.android.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.jerleo.android.DateHelper
import de.jerleo.android.R
import de.jerleo.android.activity.ActivityMain
import de.jerleo.model.Bill

internal class AdapterBillHistory(
    private var ctx: Context, textView: Int,
    private var bill: Bill,
    private var years: List<Int>
) : ArrayAdapter<Int>(ctx, textView, years) {

    private val currencyFormat: String = ActivityMain.currencyFormat

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        if (row == null) {
            val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            row = inflater.inflate(R.layout.bill_history_row, parent, false)

            val holder = ViewHolder().apply {
                year = row.findViewById(R.id.bill_year)
                fees = row.findViewById(R.id.bill_fees)
                costs = row.findViewById(R.id.bill_costs)
                payments = row.findViewById(R.id.bill_payments)
                balance = row.findViewById(R.id.bill_balance)
                textColor = balance.currentTextColor
            }
            row.tag = holder
        }

        val offset = DateHelper.today.year - years[position]
        val billFees = bill.fees(offset)
        val billCosts = bill.costs(offset)
        val billPayments = bill.payments(offset)
        val billBalance = billPayments - billFees - billCosts
        val holder = row!!.tag as ViewHolder
        holder.apply {
            year.text = (bill.dateTo.year - offset).toString()
            fees.text = String.format(currencyFormat, -billFees)
            costs.text = String.format(currencyFormat, -billCosts)
            payments.text = String.format(currencyFormat, billPayments)
            balance.text = String.format(currencyFormat, billBalance)
            if (billBalance < 0)
                balance.setTextColor(Color.RED)
            else
                balance.setTextColor(holder.textColor)
        }
        return row
    }

    internal class ViewHolder {
        lateinit var year: TextView
        lateinit var fees: TextView
        lateinit var costs: TextView
        lateinit var payments: TextView
        lateinit var balance: TextView
        var textColor = 0
    }
}