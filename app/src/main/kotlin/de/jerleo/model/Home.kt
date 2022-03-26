package de.jerleo.model

import android.util.JsonReader
import android.util.JsonWriter
import de.jerleo.database.Constants
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class Home {

    var bills: MutableList<Bill> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
            field.forEach { it.home = this }
        }

    var meters: MutableList<Meter> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
            field.forEach { it.home = this }
        }

    constructor() {
        meters = Meter.import
        bills = Bill.import
    }

    constructor(stream: InputStream?) {
        val reader = JsonReader(InputStreamReader(stream, UTF8))
        reader.beginObject()
        while (reader.hasNext())
            when (reader.nextName()) {
                Constants.METERS -> meters = Meter.import(reader)
                Constants.BILLS -> bills = Bill.import(reader)
            }
        reader.endObject()
        reader.close()
    }

    fun save(bill: Bill): Bill {
        if (!bills.contains(bill)) bills.add(bill)
        bill.persist()
        return bill
    }

    fun save(meter: Meter): Meter {
        if (!meters.contains(meter)) meters.add(meter)
        meter.persist()
        return meter
    }

    fun remove(bill: Bill) {
        bills.remove(bill)
        bill.delete()
    }

    fun remove(meter: Meter) {
        bills.forEach { it.remove(meter) }
        meters.remove(meter)
        meter.delete()
    }

    fun bill(pos: Int): Bill = bills[pos]
    fun meter(pos: Int): Meter = meters[pos]
    fun meter(id: Long): Meter? = meters.find { it.id == id }
    fun meter(num: String): Meter? = meters.find { it.number == num }

    fun load(imported: Home) {
        meters = imported.meters
        bills = imported.bills
        persist()
    }

    private fun persist() {
        meters.forEach { it.persist() }
        bills.forEach { it.persist() }
    }

    fun exportJSON(): String {
        val stream = StringWriter()
        val writer = JsonWriter(stream)
        try {
            writer.beginObject()
            if (meters.isNotEmpty()) {
                writer.name(Constants.METERS)
                writer.beginArray()
                meters.forEach { it.export(writer) }
                writer.endArray()
            }
            if (bills.isNotEmpty()) {
                writer.name(Constants.BILLS)
                writer.beginArray()
                bills.forEach { it.exportTo(writer) }
                writer.endArray()
            }
            writer.endObject()
            writer.close()
        } catch (e: IOException) {
            return ""
        }
        return stream.toString()
    }

    companion object {
        val UTF8: Charset = StandardCharsets.UTF_8
        val instance: Home = Home()
    }
}