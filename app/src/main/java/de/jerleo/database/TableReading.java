package de.jerleo.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.jerleo.android.DateHelper;
import de.jerleo.database.DatabaseColumn.DataType;
import de.jerleo.model.Reading;

import static android.provider.BaseColumns._ID;
import static de.jerleo.database.Constants.COUNT;
import static de.jerleo.database.Constants.DATE;
import static de.jerleo.database.Constants.METER;
import static de.jerleo.database.Constants.READING;

public class TableReading extends DatabaseTable {

    public TableReading(Database database) {

        super(database, READING, true);

        addColumn(new DatabaseColumn(METER, DataType.INTEGER));
        addColumn(new DatabaseColumn(DATE, DataType.TEXT));
        addColumn(new DatabaseColumn(COUNT, DataType.INTEGER));

        final DatabaseColumn meter = getColumn(METER);
        meter.setForeignKeyConstraint(true);
        meter.setForeignTable(METER);
        meter.setForeignColumn(_ID);
    }

    @Override
    public ContentValues getValues(Object object, boolean forUpdate) {

        final Reading reading = (Reading) object;

        final ContentValues values = new ContentValues();
        if (forUpdate)
            values.put(_ID, reading.getId());
        values.put(METER, reading.getMeter().getId());
        values.put(DATE, DateHelper.getDatabaseDate(reading.getDate()));
        values.put(COUNT, reading.getCount());

        return values;
    }

    @Override
    public String getWhereClause(Object object) {

        final Reading reading = (Reading) object;
        return _ID + " = " + reading.getId();
    }

    @Override
    public List<Reading> read(String condition) {

        final ArrayList<Reading> readings = new ArrayList<>();
        final String[] columns = {_ID, DATE, COUNT};
        final String where = METER + " = " + condition;
        final Cursor cursor = database.query(READING, columns, where, null,
                null, null, null);
        while (cursor.moveToNext()) {
            final Reading reading = new Reading();
            int col = 0;
            reading.setId(cursor.getLong(col++));
            reading.setDate(DateHelper.getDatabaseDate(cursor.getString(col++)));
            reading.setCount(cursor.getInt(col));
            readings.add(reading);
        }
        return readings;
    }
}