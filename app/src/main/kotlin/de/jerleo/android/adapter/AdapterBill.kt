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

internal class AdapterBill(
    private var ctx: Context, textView: Int,
    private var bills: List<Bill>
) : ArrayAdapter<Bill>(ctx, textView, bills) {

    private val currencyFormat: String = ActivityMain.currencyFormat

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

        val bill = bills[position]
        val amount = bill.balance()
        val holder = row!!.tag as ViewHolder
        holder.apply {
            description.text = bill.name
            begin.text = DateHelper.formatShort(bill.dateFrom)
            end.text = DateHelper.formatShort(bill.dateTo)
            fees.text = String.format(currencyFormat, bill.fees())
            costs.text = String.format(currencyFormat, bill.costs())
            payments.text = String.format(currencyFormat, bill.payments())
            balance.text = String.format(currencyFormat, amount)
            if (amount < 0)
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