package de.jerleo.database

import android.content.ContentValues
import android.provider.BaseColumns

abstract class Table internal constructor(
    val database: Database,
    val table: String, private
    val hasDefaultKey: Boolean
) {
    private val columns: MutableList<Column> = ArrayList()
    private val id: Column
        get() =
            Column(BaseColumns._ID, Column.Type.INTEGER).apply {
                isPrimaryKey = true
                isAutoIncrement = true
                isNullable = false
            }

    val tableSQL: String
        get() {
            val primaryKeys = ArrayList<String>()
            var foreignKey = ""
            var createSQL = "CREATE TABLE IF NOT EXISTS "
            createSQL += "$table ( "
            var comma = "  "
            for (column in columns) {
                createSQL += "\n\t$comma${column.name}"
                createSQL += " " + column.type.toString()
                if (column.isPrimaryKey)
                    if (hasDefaultKey) createSQL += " PRIMARY KEY"
                    else primaryKeys.add(column.name)
                if (column.isAutoIncrement) createSQL += " AUTOINCREMENT"
                if (!column.isNullable) createSQL += " NOT NULL"
                if (column.isUnique) createSQL += " UNIQUE"
                if (column.isForeignKeyConstraint) {
                    foreignKey += "\n\t, FOREIGN KEY (${column.name})"
                    foreignKey += " REFERENCES ${column.foreignTable}(${column.foreignColumn})"
                }
                if (comma != ", ") comma = ", "
            }
            if (!hasDefaultKey) {
                var primaryKey = ""
                comma = ""
                for (keyName in primaryKeys) {
                    primaryKey += comma + keyName
                    if (comma != ", ") comma = ", "
                }
                if (primaryKey.isNotEmpty()) createSQL += "\n\t, PRIMARY KEY ($primaryKey)"
            }
            if (foreignKey.isNotEmpty()) createSQL += foreignKey
            createSQL += "\n);"
            return createSQL
        }

    init {
        if (hasDefaultKey) columns.add(id)
    }

    abstract fun read(condition: String?): MutableList<*>
    abstract fun values(obj: Any, forUpdate: Boolean): ContentValues
    abstract fun whereClause(obj: Any): String

    fun add(column: Column) {
        if (!(hasDefaultKey && column.isPrimaryKey)) columns.add(column)
    }

    fun insert(obj: Any): Long = insert(values(obj, false))
    fun update(obj: Any): Int = update(values(obj, true), whereClause(obj))
    fun delete(obj: Any): Int = delete(whereClause(obj))

    private fun insert(values: ContentValues): Long =
        database.writableDatabase.insertOrThrow(table, null, values)

    private fun update(values: ContentValues, whereClause: String): Int =
        database.writableDatabase.update(table, values, whereClause, null)

    private fun delete(whereClause: String): Int =
        database.writableDatabase.delete(table, whereClause, null)
}