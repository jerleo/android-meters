package de.jerleo.database;

public class DatabaseColumn {

    private final String name;
    private final DataType type;

    private boolean autoIncrement = false;
    private boolean foreignKeyConstraint = false;
    private boolean nullable = true;
    private boolean primaryKey = false;

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean unique = false;

    private String foreignTable;
    private String foreignColumn;

    public DatabaseColumn(String columnName, DataType columnType) {

        this.name = columnName;
        this.type = columnType;
    }

    public String getForeignColumn() {

        return foreignColumn;
    }

    public String getForeignTable() {

        return foreignTable;
    }

    public String getName() {

        return name;
    }

    public DataType getType() {

        return type;
    }

    public boolean isAutoIncrement() {

        return autoIncrement;
    }

    public boolean isForeignKeyConstraint() {

        return foreignKeyConstraint;
    }

    public boolean isNullable() {

        return nullable;
    }

    public boolean isPrimaryKey() {

        return primaryKey;
    }

    public boolean isUnique() {

        return unique;
    }

    @SuppressWarnings("SameParameterValue")
    public void setAutoIncrement(boolean autoIncrement) {

        this.autoIncrement = autoIncrement;
    }

    @SuppressWarnings("SameParameterValue")
    public void setForeignColumn(String foreignColumn) {

        this.foreignColumn = foreignColumn;
    }

    @SuppressWarnings("SameParameterValue")
    public void setForeignKeyConstraint(boolean foreignKey) {

        this.foreignKeyConstraint = foreignKey;
    }

    public void setForeignTable(String foreignTable) {

        this.foreignTable = foreignTable;
    }

    @SuppressWarnings("SameParameterValue")
    public void setNullable(boolean nullable) {

        this.nullable = nullable;
    }

    @SuppressWarnings("SameParameterValue")
    public void setPrimaryKey(boolean primaryKey) {

        this.primaryKey = primaryKey;
    }

    public enum DataType {
        INTEGER, REAL, TEXT
    }

}