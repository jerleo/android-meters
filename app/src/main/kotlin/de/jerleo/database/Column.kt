package de.jerleo.database

class Column(val name: String, val type: Type) {
    var isAutoIncrement = false
    var isForeignKeyConstraint = false
    var isNullable = true
    var isPrimaryKey = false
    val isUnique = false
    var foreignTable: String? = null
    var foreignColumn: String? = null

    enum class Type {
        INTEGER, REAL, TEXT
    }
}