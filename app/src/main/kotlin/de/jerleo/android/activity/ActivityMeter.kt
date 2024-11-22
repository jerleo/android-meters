package de.jerleo.android.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import de.jerleo.android.R
import de.jerleo.database.Constants
import de.jerleo.model.Home
import de.jerleo.model.Meter

class ActivityMeter : FragmentActivity(), View.OnClickListener {

    private val home = Home.instance
    private var meter: Meter = Meter()

    private val name: TextView by lazy { findViewById(R.id.meter_name) }
    private val number: TextView by lazy { findViewById(R.id.meter_number) }
    private val unit: Spinner by lazy { findViewById(R.id.meter_unit) }
    private val prior: Spinner by lazy { findViewById(R.id.prior_meter) }
    private val save: Button by lazy { findViewById(R.id.save) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meter)
        setTitle(R.string.meter)
        number.addTextChangedListener(textWatcher)

        val cancel = findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener(this)
        save.setOnClickListener(this)

        var update = false
        val bundle = this.intent.extras
        bundle?.let {
            val position = bundle.getInt(Constants.METER)
            meter = home.meter(position)
            update = true
        }
        val meters = getPriorMeters(meter)
        prior.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, meters)

        meter.let { m ->
            number.text = m.number
            name.text = m.name
            unit.setSelection(m.unit.ordinal)
            m.prior?.let { p ->
                prior.setSelection(meters.indexOf(p.number))
            }
        }

        save.isEnabled = update
        unit.isEnabled = !update
        prior.isEnabled = update
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

    private fun getPriorMeters(meter: Meter): ArrayList<String> {
        val result = arrayListOf(getString(R.string.none))
        val meters: MutableList<Meter> = home.meters
        val others = meters.filter { it != meter && it.unit == meter.unit }
        result.addAll(others.map { it.number })
        return result
    }

    private val textWatcher: TextWatcher
        get() = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val entry = s.toString().trim { it <= ' ' }
                save.isEnabled = entry.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }

    private fun save() {
        let {
            home.save(meter.apply {
                this.number = it.number.text.toString().trim { it <= ' ' }
                this.name = it.name.text.toString().trim { it <= ' ' }
                this.unit = Meter.Unit.entries.toTypedArray()[it.unit.selectedItemPosition]
                this.prior = it.home.meter(it.prior.selectedItem as String)
                this.home = it.home
            })
        }
        setResult(RESULT_OK)
        finish()
    }
}