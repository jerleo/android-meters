package de.jerleo.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.jerleo.database.DatabaseColumn.DataType;
import de.jerleo.model.Meter;

import static android.provider.BaseColumns._ID;
import static de.jerleo.database.Constants.METER;
import static de.jerleo.database.Constants.NAME;
import static de.jerleo.database.Constants.NUMBER;
import static de.jerleo.database.Constants.PRIOR;
import static de.jerleo.database.Constants.UNIT;

public class TableMeter extends DatabaseTable {

    public TableMeter(Database database) {

        super(database, METER, true);

        addColumn(new DatabaseColumn(NUMBER, DataType.TEXT));
        addColumn(new DatabaseColumn(NAME, DataType.TEXT));
        addColumn(new DatabaseColumn(UNIT, DataType.TEXT));
        addColumn(new DatabaseColumn(PRIOR, DataType.INTEGER));

        final DatabaseColumn meter = getColumn(PRIOR);
        meter.setForeignKeyConstraint(true);
        meter.setForeignTable(METER);
        meter.setForeignColumn(_ID);
    }

    @Override
    public ContentValues getValues(Object object, boolean forUpdate) {

        final Meter meter = (Meter) object;

        final ContentValues values = new ContentValues();
        if (forUpdate)
            values.put(_ID, meter.getId());
        values.put(NUMBER, meter.getNumber());
        values.put(NAME, meter.getName());
        values.put(UNIT, meter.getUnit().name());
        values.put(PRIOR, meter.hasPrior() ? meter.getPrior().getId() : null);

        return values;
    }

    @Override
    public String getWhereClause(Object object) {

        final Meter meter = (Meter) object;
        return _ID + " = " + meter.getId();
    }

    @Override
    public List<Meter> read(String condition) {

        final ArrayList<Meter> meters = new ArrayList<>();
        final String[] columns = {_ID, NUMBER, NAME, UNIT, PRIOR};
        final Cursor cursor = database.query(METER, columns, condition, null,
                null, null, null);
        while (cursor.moveToNext()) {
            final Meter meter = new Meter();
            int col = 0;
            meter.setId(cursor.getLong(col++));
            meter.setNumber(cursor.getString(col++));
            meter.setName(cursor.getString(col++));
            meter.setUnit(Meter.Unit.valueOf(cursor.getString(col++)));
            meter.setPrior(cursor.getLong(col));
            meters.add(meter);
        }
        return meters;
    }

}