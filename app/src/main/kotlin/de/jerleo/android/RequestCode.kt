package de.jerleo.android

internal interface RequestCode {
    companion object {
        const val METER_CREATE = 1
        const val METER_CHANGE = 2
        const val READING_CREATE = 3
        const val READING_LIST = 4
        const val READING_CHART = 5
        const val TARIFF_LIST = 6
        const val BILL_CREATE = 7
        const val BILL_CHANGE = 8
        const val TARIFF_CHANGE = 9
        const val TARIFF_CREATE = 10
    }
}