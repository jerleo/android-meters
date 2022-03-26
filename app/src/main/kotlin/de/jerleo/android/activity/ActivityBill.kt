package de.jerleo.android.activity

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import de.jerleo.android.DateHelper
import de.jerleo.android.R
import de.jerleo.model.Bill
import de.jerleo.model.Home
import de.jerleo.model.Item

class ActivityBill : FragmentActivity(), View.OnClickListener {

    private val home = Home.instance
    private var bill: Bill = Bill()

    private val description: TextView by lazy { findViewById(R.id.description) }
    private val dateFrom: DatePicker by lazy { findViewById(R.id.begin) }
    private val meters: TableLayout by lazy { findViewById(R.id.meter_list) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bill)
        setTitle(R.string.bill)

        val save: Button = findViewById(R.id.save)
        val cancel: Button = findViewById(R.id.cancel)
        save.setOnClickListener(this)
        cancel.setOnClickListener(this)

        val bundle = this.intent.extras
        bundle?.let {
            val position = bundle.getInt("bill")
            bill = home.bill(position)
            description.text = bill.name
            dateFrom.init(
                bill.dateFrom.year, bill.dateFrom.monthValue - 1,
                bill.dateFrom.dayOfMonth, null
            )
        }
        home.meters.forEach {
            meters.addView(CheckBox(this).apply {
                this.text = String.format("%s (%s)", it.name, it.number)
                this.tag = it.number
                this.isChecked = bill.contains(it)
            })
        }
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

    private val items: MutableList<Item>
        get() {
            val items = ArrayList<Item>()
            for (idx in 0 until meters.childCount) {
                val checkBox = meters.getChildAt(idx) as CheckBox
                if (checkBox.isChecked) {
                    val item = Item()
                    item.meterNumber = checkBox.tag.toString()
                    items.add(item)
                }
            }
            return items
        }

    private fun save() {
        let {
            home.save(bill.apply {
                this.name = it.description.text.toString().trim { it <= ' ' }
                this.items = it.items
                this.dateFrom =
                    DateHelper.date(
                        it.dateFrom.year, it.dateFrom.month + 1, it.dateFrom.dayOfMonth
                    )
                this.home = it.home
            })
        }
        setResult(RESULT_OK)
        finish()
    }
}