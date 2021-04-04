package de.jerleo.model;

import android.util.JsonReader;
import android.util.JsonWriter;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.jerleo.android.DateHelper;
import de.jerleo.database.Constants;
import de.jerleo.database.Database;

public class Bill implements Comparable<Bill> {

    private Home home;
    private long id;
    private String description;
    private Calendar begin;
    private List<BillItem> items, removeItems;
    private List<Meter> meters;

    public static List<Bill> importDatabase() {

        return Database.getBillList();
    }

    public static List<Bill> importFrom(JsonReader reader) throws IOException {

        List<Bill> bills = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext())
            bills.add(Bill.importSingleFrom(reader));
        reader.endArray();

        return bills;
    }

    private static Bill importSingleFrom(JsonReader reader) throws IOException {

        Bill bill = new Bill();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();

            switch (name) {
                case Constants.DESCRIPTION:
                    bill.setDescription(reader.nextString());
                    break;
                case Constants.BEGIN:
                    bill.setBegin(DateHelper.getDatabaseDate(reader.nextString()));
                    break;
                case Constants.METERS:
                    bill.setItems(BillItem.importFrom(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return bill;
    }

    @Override
    public int compareTo(@NonNull Bill other) {

        final int compareBegin = this.begin.compareTo(other.begin);
        final int compareDescription = this.description.compareTo(other.description);
        if (compareBegin == 0)
            return compareDescription;
        else
            return compareBegin;
    }

    public void delete() {

        for (final BillItem item : items)
            item.delete();
        items.clear();

        Database.delete(this);
    }

    public void exportTo(JsonWriter writer) throws IOException {

        writer.beginObject();

        writer.name(Constants.DESCRIPTION).value(description);
        writer.name(Constants.BEGIN).value(DateHelper.getDatabaseDate(begin));

        if (items.size() > 0) {
            writer.name(Constants.METERS);
            writer.beginArray();
            for (final BillItem item : items)
                item.exportTo(writer);
            writer.endArray();
        }

        writer.endObject();
    }

    public Calendar getBegin() {

        return (Calendar) begin.clone();
    }

    public String getDescription() {

        return description;
    }

    public Calendar getEnd() {

        Calendar end = (Calendar) begin.clone();
        end.add(Calendar.YEAR, 1);
        end.add(Calendar.DAY_OF_MONTH, -1);
        return end;
    }

    public Home getHome() {

        return home;
    }

    public long getId() {

        return id;
    }

    public List<Meter> getMeters() {

        if (meters == null)
            meters = new ArrayList<>();

        meters.clear();

        for (final BillItem item : items)
            meters.add(item.getMeter());

        return meters;
    }

    public float getTotalCosts() {

        float costs = 0;
        for (final Meter meter : getMeters())
            costs += meter.getCosts(this);
        return costs;
    }

    public float getTotalFees() {

        float fees = 0;
        for (final Meter meter : getMeters())
            fees += meter.getFees(this);
        return fees;
    }

    public float getTotalPayments() {

        float payments = 0;
        for (final Meter meter : getMeters())
            payments += meter.getPayments(this);
        return payments;
    }

    public void persist() {

        if (id == 0)
            id = Database.insert(this);
        else
            Database.update(this);

        if (removeItems != null) {
            for (BillItem item : removeItems)
                Database.delete(item);
            removeItems = null;
        }

        for (BillItem item : items)
            item.persist();
    }

    public void remove(Meter meter) {

        for (BillItem item : items) {
            if (item.getMeter().equals(meter)) {
                item.delete();
                items.remove(item);
                return;
            }
        }
    }

    public void setBegin(Calendar begin) {

        this.begin = begin;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public void setHome(Home home) {

        this.home = home;
    }

    public void setId(long id) {

        this.id = id;
    }

    public void setItems(List<BillItem> newItems) {

        if (items != null) {
            removeItems = new ArrayList<>();

            for (final BillItem item : items)
                if (!newItems.contains(item))
                    removeItems.add(item);
        }
        for (final BillItem item : newItems)
            item.setBill(this);
        items = newItems;
    }
}
