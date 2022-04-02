package de.jerleo.android.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import de.jerleo.android.R
import de.jerleo.database.Constants
import de.jerleo.model.Home
import de.jerleo.model.Meter
import java.util.*

class ReadingChart : Activity() {

    private lateinit var meter: Meter

    private val averageData: LineData
        get() {
            val average: MutableList<Entry> = ArrayList()
            for (month in 1..12) {
                val x = month.toFloat() - 1
                val y = meter.averageFor(month)
                average.add(Entry(x, y))
            }
            val lineData = LineData()
            val lineDataSet = LineDataSet(average, getString(R.string.average))
            lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
            lineDataSet.color = Color.YELLOW
            lineData.addDataSet(lineDataSet)
            lineData.setDrawValues(false)

            return lineData
        }

    private val currentData: BarData
        get() {
            var date = meter.lastReading()!!.date
            val year = date.year
            val thisYear: MutableList<BarEntry> = ArrayList()
            val lastYear: MutableList<BarEntry> = ArrayList()
            repeat(12) {
                val x = date.monthValue.toFloat() - 1
                val y = meter.usage(date).toFloat()
                if (date.year == year)
                    thisYear.add(BarEntry(x, y))
                else
                    lastYear.add(BarEntry(x, y))
                date = date.minusMonths(1)
            }

            val currentData = BarDataSet(thisYear, year.toString())
            currentData.setColors(intArrayOf(R.color.chartCurrent), this)

            val priorData = BarDataSet(lastYear, (year - 1).toString())
            priorData.setColors(intArrayOf(R.color.chartPrior), this)

            val dataSets: MutableList<IBarDataSet> = ArrayList()
            dataSets.add(currentData)
            dataSets.add(priorData)

            val barData = BarData(dataSets)
            barData.setDrawValues(false)
            barData.barWidth = 0.45f

            return barData
        }

    class MonthAxisFormatter : ValueFormatter() {
        private val locale = Locale.getDefault()
        private val calendar = Calendar.getInstance()
        override fun getFormattedValue(value: Float): String {
            calendar[Calendar.MONTH] = value.toInt()
            return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale) as String
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.reading_chart)

        val bundle = this.intent.extras
        val meterPosition = bundle!!.getInt(Constants.METER)
        meter = Home.instance.meter(meterPosition)
        if (meter.lastReading() == null)
            return

        val combinedData = CombinedData()
        combinedData.setData(averageData)
        combinedData.setData(currentData)

        val chart: CombinedChart = findViewById(R.id.reading_chart)
        chart.data = combinedData

        val xAxis = chart.xAxis
        xAxis.valueFormatter = MonthAxisFormatter()
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = 11.5f
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelCount = 12
        xAxis.textColor = Color.GRAY

        chart.axisLeft.textColor = Color.GRAY
        chart.axisRight.textColor = Color.GRAY
        chart.legend.textColor = Color.WHITE
        chart.description.isEnabled = false
        chart.setScaleEnabled(false)
        chart.animateY(1500)
    }
}