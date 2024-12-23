package de.jerleo.database.table

import android.content.ContentValues
import android.provider.BaseColumns
import de.jerleo.database.Column
import de.jerleo.database.Constants
import de.jerleo.database.Database
import de.jerleo.database.Table
import de.jerleo.model.Item

class TableItem(database: Database) :
    Table(database, Constants.ITEM, true) {

    init {
        add(Column(Constants.BILL, Column.Type.INTEGER).apply {
            isForeignKeyConstraint = true
            foreignTable = Constants.BILL
            foreignColumn = BaseColumns._ID
        })
        add(Column(Constants.METER, Column.Type.INTEGER).apply {
            isForeignKeyConstraint = true
            foreignTable = Constants.METER
            foreignColumn = BaseColumns._ID
        })
    }

    override fun values(obj: Any, forUpdate: Boolean): ContentValues {
        val item = obj as Item
        val values = ContentValues()
        if (forUpdate) values.put(BaseColumns._ID, item.id)
        values.put(Constants.BILL, item.bill.id)
        values.put(Constants.METER, item.meter?.id)
        return values
    }

    override fun whereClause(obj: Any): String = BaseColumns._ID + " = " + (obj as Item).id

    override fun read(condition: String?): ArrayList<Item> {
        val items = ArrayList<Item>()
        val columns = arrayOf(BaseColumns._ID, Constants.METER)
        val whereClause = Constants.BILL + " = '" + condition + "'"
        val cursor = database.query(
            Constants.ITEM, columns, whereClause, null,
            null, null, null
        )
        while (cursor.moveToNext()) {
            var col = 0
            val item = Item()
            item.id = cursor.getLong(col++)
            item.meterId = cursor.getLong(col)
            items.add(item)
        }
        return items
    }
}