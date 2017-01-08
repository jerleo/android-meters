package de.jerleo.model;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.jerleo.database.Constants;
import de.jerleo.database.Database;

public class BillItem {

    private long id;
    private Bill bill;
    private Meter meter;
    private long meterId;
    private String meterNumber;

    public static List<BillItem> importFrom(JsonReader reader) throws IOException {

        List<BillItem> items = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext())
            items.add(importSingleFrom(reader));
        reader.endArray();

        return items;
    }

    private static BillItem importSingleFrom(JsonReader reader) throws IOException {

        BillItem item = new BillItem();

        reader.beginObject();
        while (reader.hasNext())
            if (reader.nextName().equals(Constants.METER))
                item.setMeter(reader.nextString());
            else
                reader.skipValue();
        reader.endObject();

        return item;
    }

    public void delete() {

        Database.delete(this);
    }

    public void exportTo(JsonWriter writer) throws IOException {

        writer.beginObject();
        writer.name(Constants.METER).value(getMeter().getNumber());
        writer.endObject();

    }

    public Bill getBill() {

        return bill;
    }

    public long getId() {

        return id;
    }

    public Meter getMeter() {

        if (meter == null)
            if (meterId > 0)
                meter = bill.getHome().getMeterById(meterId);
            else
                meter = bill.getHome().getMeterByNumber(meterNumber);

        return meter;
    }

    public void persist() {

        if (id == 0)
            id = Database.insert(this);
        else
            Database.update(this);
    }

    public void setBill(Bill bill) {

        this.bill = bill;
    }

    public void setId(long id) {

        this.id = id;
    }

    public void setMeter(long id) {

        this.meter = null;
        this.meterNumber = null;
        this.meterId = id;
    }

    public void setMeter(String meter) {

        this.meter = null;
        this.meterId = 0;
        this.meterNumber = meter;
    }
}
