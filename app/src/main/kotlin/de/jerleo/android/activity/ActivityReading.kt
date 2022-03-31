package de.jerleo.android.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import de.jerleo.android.DateHelper
import de.jerleo.android.R
import de.jerleo.database.Constants
import de.jerleo.model.Home
import de.jerleo.model.Meter
import de.jerleo.model.Reading
import java.text.DecimalFormat
import java.time.LocalDate

class ActivityReading : FragmentActivity(), View.OnClickListener, OnDateChangedListener {

    private val currencyFormat = ActivityMain.currencyFormat
    private val digits = ArrayList<TextView>()
    private var formatStr = ""

    private var minDate: LocalDate? = null

    private lateinit var reading: Reading
    private lateinit var meter: Meter

    private val readingDate: DatePicker by lazy { findViewById(R.id.reading_date) }
    private val readingCosts: TextView by lazy { findViewById(R.id.reading_costs) }
    private val meterUnit: TextView by lazy { findViewById(R.id.meter_unit) }
    private val save: Button by lazy { findViewById(R.id.reading_save) }
    private val minus1: View by lazy { findViewById(R.id.minus_1) }
    private val minus10: View by lazy { findViewById(R.id.minus_10) }
    private val minus100: View by lazy { findViewById(R.id.minus_100) }
    private val minus1000: View by lazy { findViewById(R.id.minus_1000) }

    private var thisCount = 0
    private var lastCount = 0
    private var viewCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reading_create)
        getReading()
        title = getString(R.string.new_reading) + ": " + meter.number
        setButtonListeners()
        setupDateListener()
        assignDigits()
        updateViews()
        updateCosts()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.plus_1000 -> thisCount += 1000
            R.id.plus_100 -> thisCount += 100
            R.id.plus_10 -> thisCount += 10
            R.id.plus_1 -> thisCount += 1
            R.id.minus_1000 -> thisCount -= 1000
            R.id.minus_100 -> thisCount -= 100
            R.id.minus_10 -> thisCount -= 10
            R.id.minus_1 -> thisCount -= 1
            R.id.reading_save -> save()
            R.id.cancel -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
        reading.count = thisCount
        if (viewCount != thisCount) {
            updateViews()
            updateCosts()
        }
    }

    override fun onDateChanged(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        reading.date = DateHelper.date(year, monthOfYear + 1, dayOfMonth)
        updateCosts()
    }

    private fun assignDigits() {
        val digits = intArrayOf(
            R.id.digit_0, R.id.digit_1, R.id.digit_2, R.id.digit_3,
            R.id.digit_4, R.id.digit_5, R.id.digit_6, R.id.digit_7
        )
        for (dig in digits) {
            val digit = findViewById<TextView>(dig)
            this.digits.add(digit)
            formatStr += "0"
        }
    }

    private fun getReading() {
        val bundle = this.intent.extras
        val position = bundle!!.getInt(Constants.METER)
        meter = Home.instance.meter(position)
        meterUnit.text = meter.unit.toString()
        reading = Reading()
        val latest = meter.latestReading()
        latest?.let {
            thisCount = it.count
            lastCount = thisCount

            // new reading must be after latest reading
            minDate = latest.date.plusDays(1)
            reading.count = thisCount
            reading.prior = latest
        }
    }

    private fun setButtonListeners() {
        val buttons = intArrayOf(
            R.id.plus_1000, R.id.plus_100, R.id.plus_10, R.id.plus_1,
            R.id.minus_1000, R.id.minus_100, R.id.minus_10, R.id.minus_1,
            R.id.reading_save, R.id.cancel
        )
        for (button in buttons) {
            val view = findViewById<View>(button)
            view.setOnClickListener(this)
        }
    }

    private fun setupDateListener() {
        val maxDate = DateHelper.today
        val year = maxDate.year
        val month = maxDate.monthValue - 1
        val day = maxDate.dayOfMonth
        reading.date = maxDate
        readingDate.minDate = if (minDate == null) 0 else DateHelper.milliSeconds(minDate!!)
        readingDate.maxDate = DateHelper.milliSeconds(maxDate)
        readingDate.init(year, month, day, this)
    }

    private fun updateCosts() {
        reading.tariff = meter.tariff(reading.date)
        readingCosts.text = String.format(currencyFormat, reading.costs())
    }

    private fun updateViews() {
        val number = DecimalFormat(formatStr).format(thisCount.toLong()).toCharArray()
        var digit = 0
        for (textView in digits) textView.setText(number, digit++, 1)

        val ok = findViewById<View>(R.id.reading_save)
        ok.isEnabled = thisCount > lastCount && readingDate.minDate <= readingDate.maxDate

        minus1000.isEnabled = thisCount - 1000 >= lastCount
        minus100.isEnabled = thisCount - 100 >= lastCount
        minus10.isEnabled = thisCount - 10 >= lastCount
        minus1.isEnabled = thisCount - 1 >= lastCount
        viewCount = thisCount
    }

    private fun save() {
        save.isEnabled = false
        meter.save(reading)
        setResult(RESULT_OK)
        finish()
    }

}