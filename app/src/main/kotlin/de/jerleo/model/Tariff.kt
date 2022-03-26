package de.jerleo.model

import android.util.JsonReader
import android.util.JsonWriter
import de.jerleo.android.DateHelper
import de.jerleo.database.Constants
import de.jerleo.database.Database
import java.io.IOException
import java.time.LocalDate

class Tariff : Comparable<Tariff> {

    var id: Long = 0L
    var fee = 0.0
    var price = 0.0
    var payment = 0.0

    lateinit var meter: Meter
    lateinit var dateFrom: LocalDate
    lateinit var dateFee: LocalDate
    lateinit var datePrice: LocalDate
    lateinit var datePayment: LocalDate

    override fun compareTo(other: Tariff): Int = dateFrom.compareTo(other.dateFrom)

    fun delete() = Database.delete(this)
    fun persist() {
        if (id == 0L) id = Database.insert(this) else Database.update(this)
    }

    fun isValidFeeFor(date: LocalDate) = hasFee() && date in dateFrom..dateFee
    fun isValidPriceFor(date: LocalDate) = hasPrice() && date in dateFrom..datePrice
    fun isValidPaymentFor(date: LocalDate) = hasPayment() && date in dateFrom..datePayment

    fun hasFee() = fee > 0
    fun hasPrice() = price > 0
    fun hasPayment() = payment > 0

    @Throws(IOException::class)
    fun exportTo(writer: JsonWriter) {
        writer.beginObject()
        writer.name(Constants.DATE).value(DateHelper.format(dateFrom))
        if (hasFee()) writer.name(Constants.FEE).value(fee.toString())
        if (hasPrice()) writer.name(Constants.PRICE).value(price.toString())
        if (hasPayment()) writer.name(Constants.PAYMENT).value(payment.toString())
        writer.endObject()
    }

    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun import(reader: JsonReader): MutableList<Tariff> {
            val tariffs: MutableList<Tariff> = ArrayList()
            reader.beginArray()
            while (reader.hasNext()) tariffs.add(importSingle(reader))
            reader.endArray()
            return tariffs
        }

        @Throws(IOException::class)
        private fun importSingle(reader: JsonReader): Tariff {
            val tariff = Tariff()
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    Constants.DATE -> tariff.dateFrom = DateHelper.parse(reader.nextString())
                    Constants.FEE -> tariff.fee = reader.nextString().toDouble()
                    Constants.PRICE -> tariff.price = reader.nextString().toDouble()
                    Constants.PAYMENT -> tariff.payment = reader.nextString().toDouble()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            return tariff
        }
    }
}