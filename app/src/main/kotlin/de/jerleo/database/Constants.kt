package de.jerleo.database

import android.provider.BaseColumns

interface Constants : BaseColumns {
    companion object {
        const val BEGIN = "begin"
        const val BILL = "bill"
        const val BILLS = "bills"
        const val COUNT = "count"
        const val DATE = "date"
        const val DESCRIPTION = "description"
        const val FEE = "fee"
        const val ITEM = "item"
        const val METER = "meter"
        const val METERS = "meters"
        const val NAME = "name"
        const val NUMBER = "number"
        const val PAYMENT = "payment"
        const val PRICE = "price"
        const val PRIOR = "prior"
        const val READING = "reading"
        const val READINGS = "readings"
        const val TARIFF = "tariff"
        const val TARIFFS = "tariffs"
        const val UNIT = "unit"
    }
}