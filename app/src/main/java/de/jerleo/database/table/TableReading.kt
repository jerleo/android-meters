package de.jerleo.database.table

import android.content.ContentValues
import android.provider.BaseColumns
import de.jerleo.android.DateHelper
import de.jerleo.database.Column
import de.jerleo.database.Constants
import de.jerleo.database.Database
import de.jerleo.database.Table
import de.jerleo.model.Reading

class TableReading(database: Database) :
    Table(database, Constants.READING, true) {

    init {
        add(Column(Constants.METER, Column.Type.INTEGER).apply {
            isForeignKeyConstraint = true
            foreignTable = Constants.METER
            foreignColumn = BaseColumns._ID
        })
        add(Column(Constants.DATE, Column.Type.TEXT))
        add(Column(Constants.COUNT, Column.Type.INTEGER))
    }

    override fun values(obj: Any, forUpdate: Boolean): ContentValues {
        val reading = obj as Reading
        val values = ContentValues()
        if (forUpdate) values.put(BaseColumns._ID, reading.id)
        values.put(Constants.METER, reading.meter.id)
        values.put(Constants.DATE, DateHelper.format(reading.date))
        values.put(Constants.COUNT, reading.count)
        return values
    }

    override fun whereClause(obj: Any): String = BaseColumns._ID + " = " + (obj as Reading).id

    override fun read(condition: String?): ArrayList<Reading> {
        val readings = ArrayList<Reading>()
        val columns = arrayOf(BaseColumns._ID, Constants.DATE, Constants.COUNT)
        val where = Constants.METER + " = " + condition
        val cursor = database.query(
            Constants.READING, columns, where, null,
            null, null, null
        )
        while (cursor.moveToNext()) {
            val reading = Reading()
            var col = 0
            reading.id = cursor.getLong(col++)
            reading.date = DateHelper.parse(cursor.getString(col++))
            reading.count = cursor.getInt(col)
            readings.add(reading)
        }
        return readings
    }
}