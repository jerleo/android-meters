package de.jerleo.model

import android.util.JsonReader
import android.util.JsonWriter
import de.jerleo.database.Constants
import de.jerleo.database.Database
import java.io.IOException

class Item {

    var id: Long = 0L
    lateinit var bill: Bill

    var meterId: Long = 0L
    var meterNumber: String? = null
    val meter by lazy {
        if (meterId != 0L) bill.home.meter(meterId)
        else meterNumber?.let { bill.home.meter(it) }
    }

    fun delete() = Database.delete(this)
    fun persist() {
        if (id == 0L) id = Database.insert(this) else Database.update(this)
    }

    fun usage(year: Int) = bill.months(year).sumOf { meter?.usage(it) ?: 0 }
    fun payments(year: Int) = bill.months(year).sumOf { meter?.payments(it) ?: 0.0 }
    fun fees(year: Int) = bill.months(year).sumOf { meter?.fees(it) ?: 0.0 }
    fun costs(year: Int) = meter?.costs(
        bill.dateFrom.withYear(year),
        bill.dateFrom.withYear(year + 1).minusDays(1)
    ) ?: 0.0

    @Throws(IOException::class)
    fun exportTo(writer: JsonWriter) {
        writer.beginObject()
        writer.name(Constants.METER).value(meter?.number)
        writer.endObject()
    }

    companion object {

        @Throws(IOException::class)
        fun import(reader: JsonReader): MutableList<Item> {
            val items: MutableList<Item> = ArrayList()
            reader.beginArray()
            while (reader.hasNext()) items.add(importSingle(reader))
            reader.endArray()
            return items
        }

        @Throws(IOException::class)
        private fun importSingle(reader: JsonReader): Item {
            val item = Item()
            reader.beginObject()
            while (reader.hasNext())
                if (reader.nextName() == Constants.METER)
                    item.meterNumber = reader.nextString()
                else
                    reader.skipValue()
            reader.endObject()
            return item
        }
    }
}