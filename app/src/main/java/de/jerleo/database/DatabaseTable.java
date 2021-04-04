package de.jerleo.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.jerleo.database.DatabaseColumn.DataType;

import static android.provider.BaseColumns._ID;

abstract public class DatabaseTable {

    final Database database;

    private final List<DatabaseColumn> columns;
    private final boolean hasDefaultKey;
    private final String tableName;

    @SuppressWarnings("SameParameterValue")
    DatabaseTable(Database database, String tableName,
                  boolean hasDefaultKey) {

        this.database = database;
        this.tableName = tableName;
        this.hasDefaultKey = hasDefaultKey;

        columns = new ArrayList<>();
        if (hasDefaultKey)
            columns.add(getId());
    }

    public void delete(Object object) {

        deleteRow(getWhereClause(object));
    }

    public String getTableName() {

        return tableName;
    }

    public String getTableSQL() {

        final ArrayList<String> primaryKeys = new ArrayList<>();
        String foreignKey = "";

        String createSQL = "CREATE TABLE IF NOT EXISTS ";
        createSQL += tableName + " ( ";

        String comma = "  ";

        for (final DatabaseColumn column : columns) {

            createSQL += "\n\t" + comma + column.getName();
            createSQL += " " + column.getType().toString();

            if (column.isPrimaryKey())
                if (hasDefaultKey)
                    createSQL += " PRIMARY KEY";
                else
                    primaryKeys.add(column.getName());

            if (column.isAutoIncrement())
                createSQL += " AUTOINCREMENT";

            if (!column.isNullable())
                createSQL += " NOT NULL";

            if (column.isUnique())
                createSQL += " UNIQUE";

            if (column.isForeignKeyConstraint())
                foreignKey += "\n\t, FOREIGN KEY (" + column.getName()
                        + ") REFERENCES " + column.getForeignTable() + "("
                        + column.getForeignColumn() + ")";

            if (!comma.equals(", "))
                comma = ", ";
        }

        if (!hasDefaultKey) {
            String primaryKey = "";
            comma = "";
            for (final String keyName : primaryKeys) {
                primaryKey += comma + keyName;
                if (!comma.equals(", "))
                    comma = ", ";
            }
            if (!primaryKey.isEmpty())
                createSQL += "\n\t, PRIMARY KEY (" + primaryKey + ")";
        }

        if (!foreignKey.isEmpty())
            createSQL += foreignKey;

        createSQL += "\n);";

        return createSQL;
    }

    public long insert(Object object) {

        final ContentValues values = getValues(object, false);
        return insertRow(values);
    }

    public abstract List<?> read(String condition);

    public void update(Object object) {

        final ContentValues values = getValues(object, true);
        updateRow(values, getWhereClause(object));
    }

    private void deleteRow(String whereClause) {

        final SQLiteDatabase db = database.getWritableDatabase();
        db.delete(tableName, whereClause, null);
    }

    private DatabaseColumn getId() {

        final DatabaseColumn id = new DatabaseColumn(_ID, DataType.INTEGER);
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);
        id.setNullable(false);
        return id;
    }

    private long insertRow(ContentValues values) {

        final SQLiteDatabase db = database.getWritableDatabase();
        return db.insertOrThrow(tableName, null, values);
    }

    private void updateRow(ContentValues values, String whereClause) {

        final SQLiteDatabase db = database.getWritableDatabase();
        db.update(tableName, values, whereClause, null);
    }

    void addColumn(DatabaseColumn column) {

        if (!(hasDefaultKey && column.isPrimaryKey()))
            columns.add(column);
    }

    DatabaseColumn getColumn(String columnName) {

        for (DatabaseColumn column : columns) {
            if (column.getName().equals(columnName))
                return column;
        }
        return null;
    }

    protected abstract ContentValues getValues(Object object, boolean forUpdate);

    protected abstract String getWhereClause(Object object);
}