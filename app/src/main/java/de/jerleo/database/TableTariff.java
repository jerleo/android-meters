package de.jerleo.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.jerleo.android.DateHelper;
import de.jerleo.database.DatabaseColumn.DataType;
import de.jerleo.model.Tariff;

import static android.provider.BaseColumns._ID;
import static de.jerleo.database.Constants.DATE;
import static de.jerleo.database.Constants.FEE;
import static de.jerleo.database.Constants.METER;
import static de.jerleo.database.Constants.PAYMENT;
import static de.jerleo.database.Constants.PRICE;
import static de.jerleo.database.Constants.TARIFF;

public class TableTariff extends DatabaseTable {

    public TableTariff(Database database) {

        super(database, TARIFF, true);

        addColumn(new DatabaseColumn(METER, DataType.INTEGER));
        addColumn(new DatabaseColumn(DATE, DataType.TEXT));
        addColumn(new DatabaseColumn(FEE, DataType.REAL));
        addColumn(new DatabaseColumn(PRICE, DataType.REAL));
        addColumn(new DatabaseColumn(PAYMENT, DataType.REAL));

        final DatabaseColumn meter = getColumn(METER);
        meter.setForeignKeyConstraint(true);
        meter.setForeignTable(METER);
        meter.setForeignColumn(_ID);
    }

    @Override
    public ContentValues getValues(Object object, boolean forUpdate) {

        final Tariff tariff = (Tariff) object;

        final ContentValues values = new ContentValues();
        if (forUpdate)
            values.put(_ID, tariff.getId());
        values.put(METER, tariff.getMeter().getId());
        values.put(DATE, DateHelper.getDatabaseDate(tariff.getValidFrom()));
        values.put(FEE, tariff.getFee());
        values.put(PRICE, tariff.getPrice());
        values.put(PAYMENT, tariff.getPayment());

        return values;
    }

    @Override
    public String getWhereClause(Object object) {

        final Tariff tariff = (Tariff) object;
        return _ID + " = " + tariff.getId();
    }

    @Override
    public List<Tariff> read(String condition) {

        final ArrayList<Tariff> tariffs = new ArrayList<>();
        final String[] columns = {_ID, DATE, FEE, PRICE, PAYMENT};
        final String where = METER + " = " + condition;
        final Cursor cursor = database.query(TARIFF, columns, where, null,
                null, null, null);
        while (cursor.moveToNext()) {
            final Tariff tariff = new Tariff();
            int col = 0;
            tariff.setId(cursor.getLong(col++));
            tariff.setValidFrom(DateHelper.getDatabaseDate(cursor.getString(col++)));
            tariff.setFee(cursor.getFloat(col++));
            tariff.setPrice(cursor.getFloat(col++));
            tariff.setPayment(cursor.getFloat(col));
            tariffs.add(tariff);
        }
        return tariffs;
    }

}