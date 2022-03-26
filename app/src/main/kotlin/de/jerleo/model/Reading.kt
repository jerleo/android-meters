package de.jerleo.model

import android.util.JsonReader
import android.util.JsonWriter
import de.jerleo.android.DateHelper
import de.jerleo.database.Constants
import de.jerleo.database.Database
import java.io.IOException
import java.time.LocalDate

class Reading : Comparable<Reading> {

    var id: Long = 0L
    var count: Int = 0

    lateinit var date: LocalDate
    lateinit var meter: Meter

    var tariff: Tariff? = null
    var prior: Reading? = null
    var isCalculated = false

    override fun compareTo(other: Reading): Int {
        val compDate = date.compareTo(other.date)
        val compCount = count.compareTo(other.count)
        return if (compDate == 0) compCount else compDate
    }

    fun delete() = Database.delete(this)
    fun persist() {
        if (isCalculated) return
        if (id == 0L) id = Database.insert(this) else Database.update(this)
    }

    fun monthEndMissing(): Boolean =
        if (prior == null || prior!!.date.monthValue == date.monthValue) false
        else prior!!.date != date.withDayOfMonth(1).minusDays(1)

    fun usage() = if (prior == null) 0 else count - prior!!.count
    fun costs() = if (tariff == null) 0.0 else tariff!!.price * usage()

    @Throws(IOException::class)
    fun exportTo(writer: JsonWriter) {
        writer.beginObject()
        writer.name(Constants.DATE).value(DateHelper.format(date))
        writer.name(Constants.COUNT).value(count.toLong())
        writer.endObject()
    }

    companion object {

        @JvmStatic
        @Throws(IOException::class)
        fun import(reader: JsonReader): MutableList<Reading> {
            val readings: MutableList<Reading> = ArrayList()
            reader.beginArray()
            while (reader.hasNext()) readings.add(importSingle(reader))
            reader.endArray()
            return readings
        }

        @Throws(IOException::class)
        private fun importSingle(reader: JsonReader): Reading {
            val reading = Reading()
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    Constants.DATE -> reading.date = DateHelper.parse(reader.nextString())
                    Constants.COUNT -> reading.count = reader.nextInt()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            return reading
        }
    }
}