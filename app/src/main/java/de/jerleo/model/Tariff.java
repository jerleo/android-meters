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

public class Tariff implements Comparable<Tariff> {

    private long id;
    private float fee;
    private float payment;
    private float price;
    private Calendar validFrom;
    private Calendar validToFee;
    private Calendar validToPayment;
    private Calendar validToPrice;
    private Meter meter;

    public static List<Tariff> importFrom(JsonReader reader) throws IOException {

        List<Tariff> tariffs = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext())
            tariffs.add(importSingleFrom(reader));
        reader.endArray();

        return tariffs;
    }

    private static Tariff importSingleFrom(JsonReader reader) throws IOException {

        Tariff tariff = new Tariff();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();

            switch (name) {
                case Constants.DATE:
                    tariff.setValidFrom(DateHelper.getDatabaseDate(reader.nextString()));
                    break;
                case Constants.FEE:
                    tariff.setFee(Float.parseFloat(reader.nextString()));
                    break;
                case Constants.PRICE:
                    tariff.setPrice(Float.parseFloat(reader.nextString()));
                    break;
                case Constants.PAYMENT:
                    tariff.setPayment(Float.parseFloat(reader.nextString()));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return tariff;
    }

    @Override
    public int compareTo(@NonNull Tariff other) {

        return validFrom.compareTo(other.validFrom);
    }

    public void delete() {

        Database.delete(this);
    }

    public void exportTo(JsonWriter writer) throws IOException {

        writer.beginObject();
        writer.name(Constants.DATE).value(DateHelper.getDatabaseDate(validFrom));

        if (fee > 0)
            writer.name(Constants.FEE).value(Float.toString(fee));

        if (price > 0)
            writer.name(Constants.PRICE).value(Float.toString(price));

        if (payment > 0)
            writer.name(Constants.PAYMENT).value(Float.toString(payment));

        writer.endObject();
    }

    public float getFee() {

        return fee;
    }

    public long getId() {

        return id;
    }

    public Meter getMeter() {

        return meter;
    }

    public float getPayment() {

        return payment;
    }

    public float getPrice() {

        return price;
    }

    public Calendar getValidFrom() {

        return (Calendar) validFrom.clone();
    }

    public boolean hasFee() {

        return fee > 0;
    }

    public boolean hasPayment() {

        return payment > 0;
    }

    public boolean hasPrice() {

        return price > 0;
    }

    public boolean isValidFeeFor(Calendar date) {

        final int compFr = validFrom.compareTo(date);
        final int compTo = validToFee.compareTo(date);
        return compFr <= 0 && compTo >= 0;
    }

    public boolean isValidPaymentFor(Calendar date) {

        final int compFr = validFrom.compareTo(date);
        final int compTo = validToPayment.compareTo(date);
        return compFr <= 0 && compTo >= 0;
    }

    public boolean isValidPriceFor(Calendar date) {

        final int compFr = validFrom.compareTo(date);
        final int compTo = validToPrice.compareTo(date);
        return compFr <= 0 && compTo >= 0;
    }

    public void persist() {

        if (id == 0)
            id = Database.insert(this);
        else
            Database.update(this);
    }

    public void setFee(float fee) {

        this.fee = fee;
    }

    public void setId(long id) {

        this.id = id;
    }

    public void setMeter(Meter meter) {

        this.meter = meter;
    }

    public void setPayment(float payment) {

        this.payment = payment;
    }

    public void setPrice(float price) {

        this.price = price;
    }

    public void setValidFrom(Calendar validFrom) {

        this.validFrom = validFrom;
    }

    public void setValidToFee(Calendar date) {

        this.validToFee = date;
    }

    public void setValidToPayment(Calendar date) {

        this.validToPayment = date;
    }

    public void setValidToPrice(Calendar date) {

        this.validToPrice = date;
    }
}