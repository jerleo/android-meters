package de.jerleo.model;

import android.util.JsonReader;
import android.util.JsonWriter;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import de.jerleo.database.Constants;

public class Home {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private List<Bill> bills;
    private List<Meter> meters;

    public Home() {

        meters = Meter.importDatabase();
        bills = Bill.importDatabase();

        updateMeters();
        updateBills();
    }

    public Home(InputStream stream) throws IOException {

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

    public String exportJSON() {

        final StringWriter stream = new StringWriter();
        final JsonWriter writer = new JsonWriter(stream);

        try {
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
        } catch (IOException e) {
            return "";
        }

        return stream.toString();
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