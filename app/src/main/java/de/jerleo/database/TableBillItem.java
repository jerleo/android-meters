package de.jerleo.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.jerleo.database.DatabaseColumn.DataType;
import de.jerleo.model.BillItem;

import static android.provider.BaseColumns._ID;
import static de.jerleo.database.Constants.BILL;
import static de.jerleo.database.Constants.ITEM;
import static de.jerleo.database.Constants.METER;

public class TableBillItem extends DatabaseTable {

    public TableBillItem(Database database) {

        super(database, ITEM, true);

        addColumn(new DatabaseColumn(BILL, DataType.INTEGER));
        addColumn(new DatabaseColumn(METER, DataType.INTEGER));

        final DatabaseColumn header = getColumn(BILL);
        header.setForeignKeyConstraint(true);
        header.setForeignTable(BILL);
        header.setForeignColumn(_ID);

        final DatabaseColumn meter = getColumn(METER);
        meter.setForeignKeyConstraint(true);
        meter.setForeignTable(METER);
        meter.setForeignColumn(_ID);
    }

    @Override
    public ContentValues getValues(Object object, boolean forUpdate) {

        final BillItem item = (BillItem) object;

        final ContentValues values = new ContentValues();
        if (forUpdate)
            values.put(_ID, item.getId());
        values.put(BILL, item.getBill().getId());
        values.put(METER, item.getMeter().getId());

        return values;
    }

    @Override
    public String getWhereClause(Object object) {

        final BillItem item = (BillItem) object;
        return _ID + " = " + item.getId();
    }

    @Override
    public List<BillItem> read(String condition) {

        final ArrayList<BillItem> items = new ArrayList<>();
        final String[] columns = {_ID, METER};
        final String whereClause = BILL + " = '" + condition + "'";
        final Cursor cursor = database.query(ITEM, columns, whereClause, null,
                null, null, null);
        while (cursor.moveToNext()) {
            int col = 0;
            final BillItem item = new BillItem();
            item.setId(cursor.getLong(col++));
            item.setMeter(cursor.getLong(col));
            items.add(item);
        }
        return items;
    }
}