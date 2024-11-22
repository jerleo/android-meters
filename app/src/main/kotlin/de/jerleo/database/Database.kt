package de.jerleo.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import de.jerleo.database.table.*
import de.jerleo.model.*

class Database private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        // Create table column definitions
        tableMeter = TableMeter(this)
        tableReading = TableReading(this)
        tableTariff = TableTariff(this)
        tableBill = TableBill(this)
        tableItem = TableItem(this)

        // Mapping between class and table
        tables = HashMap()
        tables[Meter::class.java] = tableMeter
        tables[Reading::class.java] = tableReading
        tables[Tariff::class.java] = tableTariff
        tables[Bill::class.java] = tableBill
        tables[Item::class.java] = tableItem
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=ON;")
        db.execSQL(tableMeter.tableSQL)
        db.execSQL(tableReading.tableSQL)
        db.execSQL(tableTariff.tableSQL)
        db.execSQL(tableBill.tableSQL)
        db.execSQL(tableItem.tableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        rebuild(db)
    }

    fun query(
        table: String?,
        columns: Array<String>,
        where: String?,
        args: Array<String?>?,
        groupBy: String?,
        having: String?,
        orderBy: String?
    ): Cursor = readableDatabase.query(table!!, columns, where, args, groupBy, having, orderBy)

    fun rebuild(database: SQLiteDatabase?) {
        val db = database ?: readableDatabase
        db.execSQL("DROP TABLE IF EXISTS " + tableItem.table)
        db.execSQL("DROP TABLE IF EXISTS " + tableBill.table)
        db.execSQL("DROP TABLE IF EXISTS " + tableTariff.table)
        db.execSQL("DROP TABLE IF EXISTS " + tableReading.table)
        db.execSQL("DROP TABLE IF EXISTS " + tableMeter.table)
        onCreate(db)
    }

    companion object {

        private const val DATABASE_NAME = "housemeter.db"
        private const val DATABASE_VERSION = 1

        private var database: Database? = null

        lateinit var tableBill: TableBill
        lateinit var tableItem: TableItem
        lateinit var tableMeter: TableMeter
        lateinit var tableReading: TableReading
        lateinit var tableTariff: TableTariff

        private lateinit var tables: HashMap<Any, Table>

        val bills by lazy { tableBill.import() }
        val meters by lazy { tableMeter.import() }

        fun insert(obj: Any) = tables[obj.javaClass]!!.insert(obj)
        fun update(obj: Any) = tables[obj.javaClass]!!.update(obj)
        fun delete(obj: Any) = tables[obj.javaClass]!!.delete(obj)

        fun instance(context: Context): Database? {
            if (database == null) database = Database(context)
            return database
        }
    }
}