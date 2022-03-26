package de.jerleo.database.table

import android.content.ContentValues
import android.provider.BaseColumns
import de.jerleo.android.DateHelper
import de.jerleo.database.Column
import de.jerleo.database.Constants
import de.jerleo.database.Database
import de.jerleo.database.Table
import de.jerleo.model.Bill

class TableBill(database: Database) :
    Table(database, Constants.BILL, true) {

    init {
        add(Column(Constants.DESCRIPTION, Column.Type.TEXT))
        add(Column(Constants.BEGIN, Column.Type.TEXT))
    }

    override fun values(obj: Any, forUpdate: Boolean): ContentValues {
        val header = obj as Bill
        val values = ContentValues()
        if (forUpdate) values.put(BaseColumns._ID, header.id)
        values.put(Constants.DESCRIPTION, header.name)
        values.put(Constants.BEGIN, DateHelper.format(header.dateFrom))
        return values
    }

    override fun whereClause(obj: Any): String = BaseColumns._ID + " = " + (obj as Bill).id

    override fun read(condition: String?): ArrayList<Bill> {
        val bills = ArrayList<Bill>()
        val columns = arrayOf(BaseColumns._ID, Constants.DESCRIPTION, Constants.BEGIN)
        val cursor = database.query(
            Constants.BILL, columns, condition, null, null,
            null, null
        )
        while (cursor.moveToNext()) {
            val bill = Bill()
            var col = 0
            bill.id = cursor.getLong(col++)
            bill.name = cursor.getString(col++)
            bill.dateFrom = DateHelper.parse(cursor.getString(col))
            bills.add(bill)
        }
        return bills
    }

    fun import(): MutableList<Bill> {
        val bills: MutableList<Bill> = read(null)
        for (bill in bills) {
            val id = bill.id.toString()
            val items = Database.tableItem.read(id)
            bill.items = items
        }
        return bills
    }
}