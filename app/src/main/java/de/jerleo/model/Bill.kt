package de.jerleo.model

import android.util.JsonReader
import android.util.JsonWriter
import de.jerleo.android.DateHelper
import de.jerleo.database.Constants
import de.jerleo.database.Database
import de.jerleo.database.Database.Companion.delete
import de.jerleo.database.Database.Companion.insert
import de.jerleo.database.Database.Companion.update
import java.io.IOException
import java.time.LocalDate

class Bill : Comparable<Bill> {

    var id: Long = 0L
    var name: String = ""

    lateinit var home: Home
    lateinit var dateTo: LocalDate
        private set

    var dateFrom: LocalDate = DateHelper.today
        set(value) {
            field = value
            dateTo = value.plusYears(1).minusDays(1)
        }

    var items: MutableList<Item> = ArrayList()
        set(value) {
            removed = field
            field.clear()
            field.addAll(value)
            field.forEach { it.bill = this }
        }

    private var removed: MutableList<Item> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
        }

    override fun compareTo(other: Bill): Int {
        val compareBegin = dateFrom.compareTo(other.dateFrom)
        val compareDescription = name.compareTo(other.name)
        return if (compareBegin == 0) compareDescription else compareBegin
    }

    fun usage(year:Int) = items.sumOf { it.usage(year) }
    fun payments(year: Int) = items.sumOf { it.payments(year) }
    fun fees(year: Int) = items.sumOf { it.fees(year) }
    fun costs(year: Int) = items.sumOf { it.costs(year) }

    fun delete() {
        items.forEach { it.delete() }
        items.clear()
        delete(this)
    }

    fun persist() {
        if (id == 0L) id = insert(this) else update(this)
        removed.forEach { delete(it) }
        removed.clear()
        items.forEach { it.persist() }
    }

    private fun minYear(): Int {
        val min = items.mapNotNull {
            it.meter!!.firstReading()?.date
        }.minOfOrNull { it } ?: return 0
        val from = LocalDate.of(min.year, dateFrom.month, dateFrom.dayOfMonth)
        return min.year - if (min < from) 1 else 0
    }

    private fun maxYear(): Int {
        val max = items.mapNotNull {
            it.meter!!.lastReading()?.date
        }.maxOfOrNull { it } ?: return 0
        val from = LocalDate.of(max.year, dateFrom.month, dateFrom.dayOfMonth)
        return max.year - if (max < from) 1 else 0
    }

    fun months(year: Int): MutableList<LocalDate> = DateHelper.months(
        dateFrom.withYear(year),
        DateHelper.min(
            DateHelper.today.withDayOfMonth(1),
            dateFrom.withYear(year + 1).minusDays(1)
        )
    )

    fun years(): MutableList<Int> = DateHelper.years(minYear(), maxYear())
    fun contains(meter: Meter) = items.find { it.meter == meter } != null
    fun remove(meter: Meter) {
        items.filter { it.meter == meter }.forEach {
            it.delete()
            items.remove(it)
        }
    }

    @Throws(IOException::class)
    fun exportTo(writer: JsonWriter) {
        writer.beginObject()
        writer.name(Constants.DESCRIPTION).value(name)
        writer.name(Constants.BEGIN).value(DateHelper.format(dateFrom))
        if (items.isNotEmpty()) {
            writer.name(Constants.METERS)
            writer.beginArray()
            items.forEach { it.exportTo(writer) }
            writer.endArray()
        }
        writer.endObject()
    }

    companion object {

        val import by lazy { Database.bills }

        @Throws(IOException::class)
        fun import(reader: JsonReader): MutableList<Bill> {
            val bills: MutableList<Bill> = ArrayList()
            reader.beginArray()
            while (reader.hasNext()) bills.add(importSingle(reader))
            reader.endArray()
            return bills
        }

        @Throws(IOException::class)
        private fun importSingle(reader: JsonReader): Bill {
            val bill = Bill()
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    Constants.DESCRIPTION -> bill.name = reader.nextString()
                    Constants.BEGIN -> bill.dateFrom = DateHelper.parse(reader.nextString())
                    Constants.METERS -> bill.items = Item.import(reader)
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            return bill
        }
    }
}