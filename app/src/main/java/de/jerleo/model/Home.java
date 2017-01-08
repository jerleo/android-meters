package de.jerleo.model;

import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

import de.jerleo.android.DateHelper;
import de.jerleo.database.Constants;

public class Home {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private List<Bill> bills;
    private List<Meter> meters;

    public Home() {

        meters = Meter.importDatabase();
        bills = Bill.importDatabase();

        updateMeters();
        updateBills();
    }

    public Home(File file) throws IOException {

        FileInputStream stream = new FileInputStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(stream, UTF_8));

        reader.beginObject();

        if (reader.nextName().equals(Constants.METERS))
            meters = Meter.importFrom(reader);

        if (reader.nextName().equals(Constants.BILLS))
            bills = Bill.importFrom(reader);

        reader.endObject();
        reader.close();

        updateMeters();
        updateBills();
    }

    public void add(Bill bill) {

        bill.setHome(this);
        bills.add(bill);
    }

    public void add(Meter meter) {

        meter.setHome(this);
        meters.add(meter);
    }

    public void deleteBill(Bill bill) {

        bill.delete();
        bills.remove(bill);
    }

    public void deleteMeter(Meter meter) {

        for (Bill bill : bills)
            bill.remove(meter);

        meter.delete();
        meters.remove(meter);
    }

    public void exportTo(File externalDir) throws IOException {

        final String dateTime = DateHelper.getTimestamp(DateHelper.getNow());
        final String filename = String.format("Export_%s.txt", dateTime);
        final File file = new File(externalDir, filename);
        final PrintStream stream = new PrintStream(file);
        final JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, UTF_8));

        writer.setIndent("\t");
        writer.beginObject();

        if (meters.size() > 0) {
            writer.name(Constants.METERS);
            writer.beginArray();
            for (final Meter meter : meters)
                meter.export(writer);
            writer.endArray();
        }

        if (bills.size() > 0) {
            writer.name(Constants.BILLS);
            writer.beginArray();
            for (final Bill bill : bills)
                bill.exportTo(writer);
            writer.endArray();
        }

        writer.endObject();
        writer.close();
    }

    public Bill getBill(int position) {

        return bills.get(position);
    }

    public List<Bill> getBills() {

        return bills;
    }

    public Meter getMeter(int position) {

        return meters.get(position);
    }

    public Meter getMeterById(long id) {

        for (final Meter meter : meters)
            if (meter.getId() == id)
                return meter;

        return null;
    }

    @Nullable
    public Meter getMeterByNumber(String number) {

        for (final Meter meter : meters)
            if (meter.getNumber().equals(number))
                return meter;

        return null;
    }

    public List<Meter> getMeters() {

        return meters;
    }

    public void replaceWith(Home importedHome) {

        bills.clear();
        meters.clear();

        meters.addAll(importedHome.getMeters());
        bills.addAll(importedHome.getBills());

        persist();
    }

    private void persist() {

        for (Meter meter : meters)
            meter.persist();

        for (Bill bill : bills)
            bill.persist();
    }

    private void updateBills() {

        for (Bill bill : bills)
            bill.setHome(this);
    }

    private void updateMeters() {

        for (Meter meter : meters)
            meter.setHome(this);
    }
}