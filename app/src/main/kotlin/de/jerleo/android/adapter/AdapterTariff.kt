package de.jerleo.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.jerleo.android.DateHelper
import de.jerleo.android.R
import de.jerleo.android.activity.ActivityMain
import de.jerleo.model.Tariff

internal class AdapterTariff(
    private var ctx: Context, textView: Int,
    private var tariffs: List<Tariff>
) : ArrayAdapter<Tariff>(ctx, textView, tariffs) {

    private val numberFormat = ActivityMain.numberFormat
    private val currency: String = ActivityMain.currencySymbol
    private val currencyFormat: String = ActivityMain.currencyFormat

    private val meterUnit by lazy { tariffs.first().meter.unit.toString() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        if (row == null) {
            val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            row = inflater.inflate(R.layout.tariff_row, parent, false)

            val holder = ViewHolder().apply {
                begin = row.findViewById(R.id.valid_from)
                fee = row.findViewById(R.id.monthly_fee)
                price = row.findViewById(R.id.unit_price)
                payment = row.findViewById(R.id.payment)
            }
            row.tag = holder
        }

        val tariff = tariffs[position]
        val unitPrice = tariff.price
        val monthlyFee = tariff.fee
        val paymentAmt = tariff.payment
        var unitPriceStr = ""
        var monthlyFeeStr = ""
        var paymentStr = ""
        if (unitPrice > 0)
            unitPriceStr = numberFormat.format(unitPrice) + " " + currency + "/" + meterUnit
        if (monthlyFee > 0) monthlyFeeStr = String.format(currencyFormat, monthlyFee)
        if (paymentAmt > 0) paymentStr = String.format(currencyFormat, paymentAmt)

        val holder = row!!.tag as ViewHolder
        holder.apply {
            begin.text = DateHelper.formatMedium(tariff.dateFrom)
            fee.text = monthlyFeeStr
            price.text = unitPriceStr
            payment.text = paymentStr
        }
        return row
    }

    internal class ViewHolder {
        lateinit var begin: TextView
        lateinit var fee: TextView
        lateinit var price: TextView
        lateinit var payment: TextView
    }
}