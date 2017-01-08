package de.jerleo.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.jerleo.android.DateHelper;
import de.jerleo.database.DatabaseColumn.DataType;
import de.jerleo.model.Bill;

import static android.provider.BaseColumns._ID;
import static de.jerleo.database.Constants.BEGIN;
import static de.jerleo.database.Constants.BILL;
import static de.jerleo.database.Constants.DESCRIPTION;

public class TableBill extends DatabaseTable {

    public TableBill(Database database) {

        super(database, BILL, true);

        addColumn(new DatabaseColumn(DESCRIPTION, DataType.TEXT));
        addColumn(new DatabaseColumn(BEGIN, DataType.TEXT));
    }

    @Override
    public ContentValues getValues(Object object, boolean forUpdate) {

        final Bill header = (Bill) object;

        final ContentValues values = new ContentValues();
        if (forUpdate)
            values.put(_ID, header.getId());
        values.put(DESCRIPTION, header.getDescription());
        values.put(BEGIN, DateHelper.getDatabaseDate(header.getBegin()));

        return values;
    }

    @Override
    public String getWhereClause(Object object) {

        final Bill header = (Bill) object;
        return _ID + " = " + header.getId();
    }

    @Override
    public List<Bill> read(String where) {

        final ArrayList<Bill> bills = new ArrayList<>();
        final String[] columns = {_ID, DESCRIPTION, BEGIN};
        final Cursor cursor = database.query(BILL, columns, where, null, null,
                null, null);
        while (cursor.moveToNext()) {
            final Bill bill = new Bill();
            int col = 0;
            bill.setId(cursor.getLong(col++));
            bill.setDescription(cursor.getString(col++));
            bill.setBegin(DateHelper.getDatabaseDate(cursor.getString(col)));
            bills.add(bill);
        }
        return bills;
    }

}