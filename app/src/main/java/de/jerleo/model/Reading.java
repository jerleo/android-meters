package de.jerleo.model;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.jerleo.android.DateHelper;
import de.jerleo.database.Constants;
import de.jerleo.database.Database;

public class Reading implements Comparable<Reading> {

    private long id;
    private int count;
    private Calendar date;
    private Meter meter;
    private Reading prior;
    private Tariff tariff;
    private boolean calculated = false;

    public static List<Reading> importFrom(JsonReader reader) throws IOException {

        List<Reading> readings = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext())
            readings.add(Reading.importSingleFrom(reader));
        reader.endArray();

        return readings;
    }

    private static Reading importSingleFrom(JsonReader reader) throws IOException {

        Reading reading = new Reading();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case Constants.DATE:
                    reading.setDate(DateHelper.getDatabaseDate(reader.nextString()));
                    break;
                case Constants.COUNT:
                    reading.setCount(reader.nextInt());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return reading;
    }

    @Override
    public int compareTo(@NonNull Reading other) {

        final int compDate = this.date.compareTo(other.date);
        final int compCount = Integer.valueOf(this.count)
                .compareTo(other.count);
        if (compDate == 0)
            return compCount;
        else
            return compDate;
    }

    public void delete() {

        Database.delete(this);
    }

    public void exportTo(JsonWriter writer) throws IOException {

        writer.beginObject();
        writer.name(Constants.DATE).value(DateHelper.getDatabaseDate(date));
        writer.name(Constants.COUNT).value(count);
        writer.endObject();
    }

    public int getConsumption() {

        if (prior == null)
            return 0;
        else
            return this.count - prior.count;
    }

    public float getCosts() {

        if (tariff == null)
            return 0;
        else
            return tariff.getPrice() * getConsumption();
    }

    public int getCount() {

        return count;
    }

    public Calendar getDate() {

        return (Calendar) date.clone();
    }

    public long getId() {

        return id;
    }

    public Meter getMeter() {

        return meter;
    }

    public String getMonth() {

        return DateHelper.getMonthKey(date);
    }

    public Reading getPrior() {

        return prior;
    }

    public boolean isBetween(Calendar start, Calendar end) {

        return start.compareTo(date) <= 0 && end.compareTo(date) >= 0;
    }

    public boolean isCalculated() {

        return calculated;
    }

    public void persist() {

        if (calculated)
            return;

        if (id == 0)
            id = Database.insert(this);
        else
            Database.update(this);
    }

    @SuppressWarnings("SameParameterValue")
    public void setCalculated(boolean calculated) {

        this.calculated = calculated;
    }

    public void setCount(int count) {

        this.count = count;
    }

    public void setDate(Calendar date) {

        this.date = date;
    }

    public void setId(long id) {

        this.id = id;
    }

    public void setMeter(Meter meter) {

        this.meter = meter;
    }

    public void setPriceFrom(List<Tariff> tariffs) {

        this.tariff = null;
        if (tariffs != null)
            for (final Tariff tariff : tariffs)
                if (tariff.hasPrice() && tariff.isValidPriceFor(date)) {
                    this.tariff = tariff;
                    return;
                }
    }

    public void setPrior(Reading prior) {

        this.prior = prior;
    }
}