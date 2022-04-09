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

class Meter : Comparable<Meter> {

    lateinit var home: Home

    var name: String = ""
    var number: String = ""
    var unit: Unit = Unit.KWH

    var id: Long = 0L
    var priorId: Long = 0
    var priorNumber: String? = null
    var prior: Meter? = null
        get() {
            if (field == null)
                field = if (priorId > 0) home.meter(priorId) else
                    priorNumber?.let { home.meter(it) }
            return field
        }
        set(value) {
            if (field == value) return
            priorId = value?.id ?: 0
            priorNumber = value?.number
            field = value
        }

    var readings: MutableList<Reading> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
            updateReadings()
        }

    var tariffs: MutableList<Tariff> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
            updateTariffs()
        }

    override fun compareTo(other: Meter): Int = number.compareTo(other.number)
    override fun toString(): String = number + ";" + name + ";" + unit.name

    fun tariff(pos: Int): Tariff = tariffs[pos]
    fun tariff(date: LocalDate) = tariffs.firstOrNull { it.isValidPriceFor(date) }

    fun save(reading: Reading): Reading {
        val that = this
        readings.add(0,
            reading.apply {
                meter = that
                tariff = tariff(reading.date)
                prior = lastReading()
            })
        reading.persist()
        update()
        return reading
    }

    fun save(tariff: Tariff): Tariff {
        if (!tariffs.contains(tariff)) tariffs.add(tariff)
        tariff.persist()
        updateTariffs()
        update()
        return tariff
    }

    fun delete() {
        tariffs.forEach { it.delete() }
        tariffs.clear()
        readings.forEach { it.delete() }
        readings.clear()
        delete(this)
    }

    fun delete(tariff: Tariff) {
        Database.delete(tariff)
        tariffs.remove(tariff)
        updateTariffs()
    }

    fun deleteLastReading() {
        if (readings.isEmpty()) return
        val reading = readings.first()
        val prior = reading.prior
        delete(reading)
        readings.removeAt(0)
        if (prior != null && prior.isCalculated) readings.remove(prior)
    }

    @Throws(IOException::class)
    fun export(writer: JsonWriter) {
        writer.beginObject()
        writer.name(Constants.NAME).value(name)
        writer.name(Constants.NUMBER).value(number)
        writer.name(Constants.UNIT).value(unit.toString())
        prior?.let { writer.name(Constants.PRIOR).value(prior?.number) }
        if (tariffs.isNotEmpty()) {
            writer.name(Constants.TARIFFS)
            writer.beginArray()
            tariffs.forEach { it.exportTo(writer) }
            writer.endArray()
        }
        if (readings.isNotEmpty()) {
            writer.name(Constants.READINGS)
            writer.beginArray()
            readings.filter { !it.isCalculated }.forEach { it.exportTo(writer) }
            writer.endArray()
        }
        writer.endObject()
    }

    fun averageFor(month: Int): Float {
        var readings = readings.filter { it.date.monthValue == month }
        var months = readings.map { it.date.withDayOfMonth(1) }.distinct().count()
        var usage = readings.sumOf { it.usage() }.toFloat()
        prior?.let { meter ->
            readings = meter.readings.filter { it.date.monthValue == month }
            months += readings.map { it.date.withDayOfMonth(1) }.distinct().count()
            usage += readings.sumOf { it.usage() }.toFloat()
        }
        return if (months > 0) usage / months else 0f
    }

    fun usage(month: LocalDate) =
        readings.filter {
            it.date.year == month.year && it.date.monthValue == month.monthValue
        }.sumOf { it.usage() }

    fun payments(month: LocalDate): Double =
        tariffs.firstOrNull { it.isValidPaymentFor(month) }?.payment ?: 0.0

    fun costs(from: LocalDate, to: LocalDate): Double =
        readings.filter { it.date in from..to }.sumOf { it.costs() }

    fun fees(month: LocalDate): Double =
        tariffs.firstOrNull { it.isValidFeeFor(month) }?.fee ?: 0.0

    fun firstReading(): Reading? = readings.lastOrNull()
    fun lastReading(): Reading? = readings.firstOrNull()

    fun persist() {
        if (id == 0L) id = insert(this) else update(this)
        readings.forEach { it.persist() }
        tariffs.forEach { it.persist() }
    }

    fun update() {
        addCalculatedReadings()
        updateReadings()
    }

    private fun addCalculatedReadings() {
        val calculated: MutableList<Reading> = ArrayList()
        readings.filter { it.monthEndMissing() }.forEach { calculated.add(interpolated(it)) }
        readings.addAll(calculated)
    }

    private fun interpolated(reading: Reading): Reading {

        // Get reading dates
        val thisDate = reading.date
        val lastDate = reading.prior!!.date
        val endMonth = reading.date.withDayOfMonth(1).minusDays(1)

        // Calculate total days between readings
        val daysTotal = DateHelper.daysBetween(lastDate, thisDate)

        // Calculate days between current reading and end of month
        val daysToLast = DateHelper.daysBetween(endMonth, thisDate)

        // Calculate interpolated meter count
        val usageTotal = (reading.count - reading.prior!!.count).toFloat()
        val usagePerDay = usageTotal / daysTotal
        val newCounter = reading.count - usagePerDay * daysToLast

        // Get current meter
        val that = this

        // Return new interpolated reading
        return Reading().apply {
            meter = that
            date = endMonth
            count = newCounter.toInt()
            isCalculated = true
            tariff = tariff(date)
            prior = reading.prior
            reading.prior = this
        }
    }

    private fun updateTariffs() {
        var validToFee = DateHelper.today
        var validToPrice = DateHelper.today
        var validToPayment = DateHelper.today

        var lastFee = 0.0
        var lastPrice = 0.0
        var lastPayment = 0.0

        tariffs.sorted().forEach {
            if (it.hasFee()) lastFee = it.fee else it.lastFee = lastFee
            if (it.hasPrice()) lastPrice = it.price else it.lastPrice = lastPrice
            if (it.hasPayment()) lastPayment = it.payment else it.lastPayment = lastPayment
        }
        tariffs.sortDescending()
        tariffs.forEach {
            if (it.hasFee()) {
                it.dateFee = validToFee
                validToFee = it.dateFrom.minusDays(1)
            }
            if (it.hasPrice()) {
                it.datePrice = validToPrice
                validToPrice = it.dateFrom.minusDays(1)
            }
            if (it.hasPayment()) {
                it.datePayment = validToPayment
                validToPayment = it.dateFrom.minusDays(1)
            }
            it.meter = this
        }
    }

    private fun updateReadings() {
        var prior: Reading? = null
        readings.sort()
        readings.forEach {
            it.meter = this
            it.tariff = tariff(it.date)
            it.prior = prior
            prior = it
        }
        readings.sortDescending()
    }

    enum class Unit {
        KWH {
            override fun toString(): String = "kWh"
        },
        M3 {
            override fun toString(): String = "m³"
        }
    }

    companion object {

        val import by lazy { Database.meters }

        @Throws(IOException::class)
        fun import(reader: JsonReader): MutableList<Meter> {
            val meters: MutableList<Meter> = ArrayList()
            reader.beginArray()
            while (reader.hasNext()) meters.add(importSingle(reader))
            reader.endArray()
            return meters
        }

        @Throws(IOException::class)
        private fun importSingle(reader: JsonReader): Meter {
            val meter = Meter()
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    Constants.NAME -> meter.name = reader.nextString()
                    Constants.NUMBER -> meter.number = reader.nextString()
                    Constants.UNIT -> {
                        val unitString = reader.nextString()
                        meter.unit = Unit.values().find { unitString == it.toString() } ?: Unit.M3
                    }
                    Constants.PRIOR -> meter.priorNumber = reader.nextString()
                    Constants.TARIFFS -> meter.tariffs = Tariff.import(reader)
                    Constants.READINGS -> meter.readings = Reading.import(reader)
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            meter.update()
            return meter
        }
    }
}