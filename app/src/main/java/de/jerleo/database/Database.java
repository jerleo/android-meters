package de.jerleo.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.List;

import de.jerleo.model.Bill;
import de.jerleo.model.BillItem;
import de.jerleo.model.Meter;
import de.jerleo.model.Reading;
import de.jerleo.model.Tariff;

public final class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "housemeter.db";
    private static final int DATABASE_VERSION = 1;

    private static Database database;

    private static TableBill bill;
    private static TableBillItem billItem;
    private static TableMeter meter;
    private static TableReading reading;
    private static TableTariff tariff;

    private static HashMap<Object, DatabaseTable> tables;

    private Database(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // Create table column definitions
        meter = new TableMeter(this);
        reading = new TableReading(this);
        tariff = new TableTariff(this);
        bill = new TableBill(this);
        billItem = new TableBillItem(this);

        // Mapping between class and table
        tables = new HashMap<>();
        tables.put(Meter.class, meter);
        tables.put(Reading.class, reading);
        tables.put(Tariff.class, tariff);
        tables.put(Bill.class, bill);
        tables.put(BillItem.class, billItem);
    }

    public static void delete(Object object) {

        final DatabaseTable table = tables.get(object.getClass());
        table.delete(object);
    }

    public static List<Bill> getBillList() {

        final List<Bill> bills = bill.read(null);
        for (final Bill bill : bills) {

            String billId = Long.toString(bill.getId());
            List<BillItem> billItems = billItem.read(billId);
            bill.setItems(billItems);
        }
        return bills;
    }

    public static Database getInstance(Context context) {

        if (database == null)
            database = new Database(context);
        return database;
    }

    public static List<Meter> getMeterList() {

        final List<Meter> meters = meter.read(null);
        for (final Meter meter : meters) {

            String meterId = Long.toString(meter.getId());
            List<Tariff> tariffs = tariff.read(meterId);
            List<Reading> readings = reading.read(meterId);

            meter.setTariffs(tariffs);
            meter.setReadings(readings);
            meter.update();
        }
        return meters;
    }

    public static long insert(Object object) throws SQLiteConstraintException {

        final DatabaseTable table = tables.get(object.getClass());
        return table.insert(object);
    }

    public static void update(Object object) {

        final DatabaseTable table = tables.get(object.getClass());
        table.update(object);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL(meter.getTableSQL());
        db.execSQL(reading.getTableSQL());
        db.execSQL(tariff.getTableSQL());
        db.execSQL(bill.getTableSQL());
        db.execSQL(billItem.getTableSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        rebuild(db);
    }

    @SuppressWarnings("SameParameterValue")
    public Cursor query(String tableName, String[] columns, String where,
                        String[] args, String groupBy, String having, String orderBy) {

        final SQLiteDatabase db = getReadableDatabase();
        return db.query(tableName, columns, where, args, groupBy, having,
                orderBy);
    }

    public void rebuild(SQLiteDatabase database) {

        final SQLiteDatabase db = database == null ? getReadableDatabase() : database;

        db.execSQL("DROP TABLE IF EXISTS " + billItem.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + bill.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + tariff.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + reading.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + meter.getTableName());
        onCreate(db);
    }
}