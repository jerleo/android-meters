package de.jerleo.android.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.DatePicker.OnDateChangedListener
import de.jerleo.android.DateHelper
import de.jerleo.android.R
import de.jerleo.database.Constants
import de.jerleo.model.Home
import de.jerleo.model.Meter
import de.jerleo.model.Tariff
import java.time.LocalDate

class ActivityTariff : Activity(),
    View.OnClickListener,
    CompoundButton.OnCheckedChangeListener,
    OnDateChangedListener {

    private val numberFormat = ActivityMain.numberFormat
    private var tariff: Tariff = Tariff()
    private lateinit var meter: Meter

    private val datePicker: DatePicker by lazy { findViewById(R.id.valid_from) }
    private val unitPrice: TextView by lazy { findViewById(R.id.unit_price) }
    private val monthFee: TextView by lazy { findViewById(R.id.monthly_fee) }
    private val monthPay: TextView by lazy { findViewById(R.id.payment) }
    private val hasFee: CheckBox by lazy { findViewById(R.id.has_fee) }
    private val hasPrice: CheckBox by lazy { findViewById(R.id.has_price) }
    private val hasPayment: CheckBox by lazy { findViewById(R.id.has_pay) }
    private val save: Button by lazy { findViewById(R.id.save) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tariff)
        setTitle(R.string.tariff)

        hasFee.setOnCheckedChangeListener(this)
        hasPrice.setOnCheckedChangeListener(this)
        hasPayment.setOnCheckedChangeListener(this)

        val cancel = findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener(this)
        save.setOnClickListener(this)

        var tariffPosition = -1
        val bundle = this.intent.extras
        bundle?.let {
            if (it.containsKey(Constants.TARIFF))
                tariffPosition = it.getInt(Constants.TARIFF)

            val meterId = it.getLong(Constants.METER)
            val meter = Home.instance.meter(meterId)
            meter?.let {
                this.meter = it

                if (tariffPosition >= 0) {
                    tariff = it.tariff(tariffPosition)
                    datePicker.init(
                        tariff.dateFrom.year, tariff.dateFrom.monthValue - 1,
                        tariff.dateFrom.dayOfMonth, this
                    )

                    if (tariff.hasFee()) {
                        hasFee.isChecked = true
                        monthFee.isEnabled = true
                        monthFee.text = numberFormat.format(tariff.fee)
                    }

                    if (tariff.hasPrice()) {
                        hasPrice.isChecked = true
                        unitPrice.isEnabled = true
                        unitPrice.text = numberFormat.format(tariff.price)
                    }

                    if (tariff.hasPayment()) {
                        hasPayment.isChecked = true
                        monthPay.isEnabled = true
                        monthPay.text = numberFormat.format(tariff.payment)
                    }
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.has_fee -> monthFee.isEnabled = isChecked
            R.id.has_pay -> monthPay.isEnabled = isChecked
            R.id.has_price -> unitPrice.isEnabled = isChecked
        }
        save.isEnabled = hasFee.isChecked || hasPrice.isChecked || hasPayment.isChecked
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.save -> save()
            R.id.cancel -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onDateChanged(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        tariff.dateFrom = LocalDate.of(year, monthOfYear, dayOfMonth)
    }

    private fun parse(view: TextView): Double =
        if (view.text.isEmpty()) 0.0
        else view.text.toString().replace(",", ".").toDouble()

    private fun save() {
        val fee = if (hasFee.isChecked) parse(monthFee) else 0.0
        val pay = if (hasPayment.isChecked) parse(monthPay) else 0.0
        val price = if (hasPrice.isChecked) parse(unitPrice) else 0.0
        val dateFrom = DateHelper.date(
            datePicker.year, datePicker.month + 1,
            datePicker.dayOfMonth
        )
        let {
            meter.save(tariff.apply {
                this.dateFrom = dateFrom
                this.fee = fee
                this.price = price
                this.payment = pay
                this.meter = it.meter
            })
        }
        setResult(RESULT_OK)
        finish()
    }
}