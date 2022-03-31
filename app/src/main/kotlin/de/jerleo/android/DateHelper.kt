package de.jerleo.android

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit.DAYS

object DateHelper {

    private val formatLong = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
    private val formatMedium = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val formatTime = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss")

    val today: LocalDate = LocalDate.now()

    private var lastFr: LocalDate? = null
    private var lastTo: LocalDate? = null
    private var lastMonths: MutableList<LocalDate>? = null

    fun format(date: LocalDate): String = date.format(DateTimeFormatter.ISO_DATE)
    fun formatLong(date: LocalDate): String = date.format(formatLong)
    fun formatMedium(date: LocalDate): String = date.format(formatMedium)

    fun parse(dateString: String): LocalDate = LocalDate.parse(dateString)

    fun date(year: Int, month: Int, day: Int): LocalDate = LocalDate.of(year, month, day)
    fun daysBetween(start: LocalDate, end: LocalDate): Int = DAYS.between(start, end).toInt()

    fun timestamp(): String = LocalDateTime.now().format(formatTime)
    fun milliSeconds(date: LocalDate) =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun min(date1: LocalDate, date2: LocalDate): LocalDate = if (date1 < date2) date1 else date2

    fun months(from: LocalDate, to: LocalDate): MutableList<LocalDate> {
        if (from == lastFr && to == lastTo)
            return lastMonths!!
        val result: ArrayList<LocalDate> = ArrayList()
        var month = from
        while (month <= to) {
            result.add(month)
            month = month.plusMonths(1)
        }
        lastFr = from
        lastTo = to
        lastMonths = result
        return result
    }

    fun years(from: LocalDate, to: LocalDate): MutableList<Int> {
        val result: ArrayList<Int> = ArrayList()
        val years = to.year - from.year
        if (years > 0)
            repeat(years) {
                result.add(to.minusYears(it.toLong()).year)
            }
        return result
    }
}