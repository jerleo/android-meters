package de.jerleo.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import de.jerleo.android.DateHelper
import de.jerleo.android.DialogHelper
import de.jerleo.android.R
import de.jerleo.android.activity.ActivityMain
import de.jerleo.android.list.ListMeter
import de.jerleo.model.Meter
import java.text.DecimalFormat

internal class AdapterMeter(
    private var ctx: Context, textView: Int,
    private var meters: List<Meter>,
    private var listMeter: ListMeter
) : ArrayAdapter<Meter>(ctx, textView, meters), DialogHelper.OnListChangedListener {

    private var decimalFormat: DecimalFormat = ActivityMain.decimalFormat

    override fun getCount() = meters.size

    override fun getItem(position: Int): Meter? {
        return meters[position]
    }

    override fun onListChanged() {
        meters = (ctx as ActivityMain).metersFiltered()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        if (row == null) {
            val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            row = inflater.inflate(R.layout.meter_row, parent, false)

            val holder = ViewHolder().apply {
                name = row.findViewById(R.id.meter_name)
                number = row.findViewById(R.id.meter_number)
                date = row.findViewById(R.id.meter_date)
                count = row.findViewById(R.id.meter_count)

                chart = row.findViewById(R.id.chart_button)
                chart.setOnClickListener { v: View ->
                    listMeter.showChart(v.tag as Long)
                }

                more = row.findViewById(R.id.more_button)
                more.setOnClickListener { v: View ->
                    listMeter.showReadings(v.tag as Long)
                }
            }
            row.tag = holder
        }
        val holder = row!!.tag as ViewHolder
        holder.apply {

            var dateTxt: String = ""
            var countTxt: String = ""

            val meter = meters[position]
            val last = meter.lastReading()
            if (last != null) {
                countTxt = decimalFormat.format(last.count) + " " + meter.unit
                dateTxt = DateHelper.formatLong(last.date)
            }
            number.text = meter.number
            name.text = meter.name
            date.text = dateTxt
            count.text = countTxt
            chart.tag = meter.id
            more.tag = meter.id
        }
        return row
    }

    internal class ViewHolder {
        lateinit var name: TextView
        lateinit var number: TextView
        lateinit var count: TextView
        lateinit var date: TextView
        lateinit var chart: ImageButton
        lateinit var more: ImageButton
    }
}