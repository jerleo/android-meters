package de.jerleo.model;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.jerleo.android.DateHelper;
import de.jerleo.database.Constants;
import de.jerleo.database.Database;


public class Meter {

    private final List<Reading> readings = new ArrayList<>();
    private final List<Tariff> tariffs = new ArrayList<>();

    private Home home;

    private long id;
    private String name;
    private String number;
    private Unit unit;

    private Meter prior;
    private long priorId;
    private String priorNumber;

    private HashMap<String, Integer> monthlyConsumption;

    public static List<Meter> importDatabase() {

        return Database.getMeterList();
    }

    public static List<Meter> importFrom(JsonReader reader) throws IOException {

        List<Meter> meters = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext())
            meters.add(Meter.importSingleFrom(reader));
        reader.endArray();

        return meters;
    }

    private static Meter importSingleFrom(JsonReader reader) throws IOException {

        Meter meter = new Meter();

        reader.beginObject();
        while (reader.hasNext()) {

            String nextName = reader.nextName();

            switch (nextName) {
                case Constants.NAME:
                    meter.setName(reader.nextString());
                    break;
                case Constants.NUMBER:
                    meter.setNumber(reader.nextString());
                    break;
                case Constants.UNIT:
                    meter.setUnit(Unit.valueOf(reader.nextString()));
                    break;
                case Constants.PRIOR:
                    meter.setPrior(reader.nextString());
                    break;
                case Constants.READINGS:
                    meter.setReadings(Reading.importFrom(reader));
                    break;
                case Constants.TARIFFS:
                    meter.setTariffs(Tariff.importFrom(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        meter.update();
        return meter;
    }

    public void add(Reading reading) {

        reading.setMeter(this);
        reading.setPriceFrom(tariffs);
        reading.setPrior(getLastReading());
        reading.setId(Database.insert(reading));
        readings.add(0, reading);

        addCalculatedReadings();
        updateConsumption();
    }

    public void add(Tariff tariff) {

        tariff.setMeter(this);
        tariffs.add(tariff);
    }

    public void delete() {

        for (final Tariff tariff : tariffs)
            tariff.delete();
        tariffs.clear();

        for (final Reading reading : readings)
            reading.delete();
        readings.clear();

        Database.delete(this);
    }

    public void delete(Tariff tariff) {

        Database.delete(tariff);
        tariffs.remove(tariff);
        updateTariffValidity();
        updateConsumption();
    }

    public void deleteLastReading() {

        if (readings.size() == 0)
            return;

        final Reading reading = readings.get(0);
        final Reading prior = reading.getPrior();

        Database.delete(reading);
        readings.remove(0);

        if (prior != null && prior.isCalculated())
            readings.remove(prior);

    }

    public void export(JsonWriter writer) throws IOException {

        writer.beginObject();

        writer.name(Constants.NAME).value(name);
        writer.name(Constants.NUMBER).value(number);
        writer.name(Constants.UNIT).value(unit.name());

        if (hasPrior())
            writer.name(Constants.PRIOR).value(getPrior().getNumber());

        if (readings.size() > 0) {
            writer.name(Constants.READINGS);
            writer.beginArray();
            for (final Reading reading : readings)
                if (!reading.isCalculated())
                    reading.exportTo(writer);
            writer.endArray();
        }

        if (tariffs.size() > 0) {
            writer.name(Constants.TARIFFS);
            writer.beginArray();
            for (final Tariff tariff : tariffs)
                tariff.exportTo(writer);
            writer.endArray();
        }

        writer.endObject();
    }

    public float getAverageFor(final String month) {

        int sum = 0;
        int count = 0;

        for (String monthKey : monthlyConsumption.keySet())
            if (monthKey.substring(monthKey.length() - 2).equals(month)) {
                sum += monthlyConsumption.get(monthKey);
                count++;
            }

        if (hasPrior()) {
            HashMap<String, Integer> priorConsumption = getPrior().getMonthlyConsumption();
            for (String monthKey : priorConsumption.keySet())
                if (monthKey.substring(monthKey.length() - 2).equals(month)) {
                    sum += priorConsumption.get(monthKey);
                    count++;
                }
        }
        return count > 0 ? (float) sum / count : 0;
    }

    public int getConsumptionFor(Calendar month) {

        String monthKey = DateHelper.getMonthKey(month);

        return monthlyConsumption.containsKey(monthKey) ?
                monthlyConsumption.get(monthKey) : 0;
    }

    public float getCosts(Bill bill) {

        float costs = 0;
        final Calendar start = bill.getBegin();
        final Calendar end = bill.getEnd();
        for (final Reading r : readings)
            if (r.isBetween(start, end))
                costs += r.getCosts();
        return costs;
    }

    public float getFees(Bill bill) {

        final Calendar thisMonth = DateHelper.getToday();
        thisMonth.set(Calendar.DAY_OF_MONTH, 1);
        final Calendar firstMonth = bill.getBegin();
        final Calendar lastMonth = bill.getEnd().after(thisMonth) ?
                thisMonth : bill.getEnd();

        float totalFees = 0;
        while (lastMonth.compareTo(firstMonth) >= 0) {
            totalFees += getFeeForMonth(lastMonth);
            lastMonth.add(Calendar.MONTH, -1);
        }
        return totalFees;
    }

    public long getId() {

        return id;
    }

    public Reading getLastReading() {

        if (readings.size() > 0)
            return readings.get(0);
        else
            return null;
    }

    public String getName() {

        return name;
    }

    public String getNumber() {

        return number;
    }

    public float getPayments(Bill bill) {

        final Calendar thisMonth = DateHelper.getToday();
        thisMonth.set(Calendar.DAY_OF_MONTH, 1);
        final Calendar firstMonth = bill.getBegin();
        final Calendar lastMonth = bill.getEnd().after(thisMonth) ?
                thisMonth : bill.getEnd();

        float totalPayments = 0;
        while (lastMonth.compareTo(firstMonth) >= 0) {
            totalPayments += getPaymentForMonth(lastMonth);
            lastMonth.add(Calendar.MONTH, -1);
        }

        return totalPayments;
    }

    public Meter getPrior() {

        if (prior == null)
            if (priorId > 0)
                prior = home.getMeterById(priorId);
            else
                prior = home.getMeterByNumber(priorNumber);

        return prior;
    }

    public List<Reading> getReadings() {

        return readings;
    }

    public Tariff getTariff(int position) {

        return tariffs.get(position);
    }

    public List<Tariff> getTariffs() {

        return tariffs;
    }

    public Unit getUnit() {

        return unit;
    }

    public boolean hasPrior() {

        return getPrior() != null;
    }

    public void persist() {

        if (id == 0)
            id = Database.insert(this);
        else
            Database.update(this);

        for (Reading reading : readings)
            reading.persist();

        for (Tariff tariff : tariffs)
            tariff.persist();
    }

    public void setHome(Home home) {

        this.home = home;
    }

    public void setId(long id) {

        this.id = id;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void setNumber(String number) {

        this.number = number;
    }

    public void setPrior(String number) {

        this.prior = null;
        this.priorId = 0;
        this.priorNumber = number;
    }

    public void setPrior(long id) {

        this.prior = null;
        this.priorNumber = null;
        this.priorId = id;
    }

    public void setReadings(List<Reading> newReadings) {

        readings.clear();
        readings.addAll(newReadings);
        addCalculatedReadings();
    }

    public void setTariffs(List<Tariff> newTariffs) {

        tariffs.clear();
        tariffs.addAll(newTariffs);
        for (final Tariff tariff : tariffs)
            tariff.setMeter(this);
    }

    public void setUnit(Unit unit) {

        this.unit = unit;
    }

    @Override
    public String toString() {

        return number + ";" + name + ";" + unit.name();
    }

    public void update() {

        updateTariffValidity();
        updateConsumption();
    }

    private void addCalculatedReadings() {

        List<Reading> calcReadings = new ArrayList<>();

        // Sort ascending by date
        Collections.sort(readings);

        // Initialize month
        int month = -1;

        // First reading has no priorNumber reading
        Reading prior = null;

        for (final Reading reading : readings) {

            // Get current month
            int currentMonth = reading.getDate().get(Calendar.MONTH);

            // Check for change of month ignoring first month
            if (month > -1 && month != currentMonth) {

                // Calculate interpolation
                Reading calculated = getInterpolation(reading, prior);

                // Check for insertion
                if (!calculated.equals(prior))
                    calcReadings.add(calculated);

                // Set priorNumber reading
                prior = calculated;
            }

            // Remember new month
            month = currentMonth;

            // Set reading attributes
            reading.setMeter(this);
            reading.setPrior(prior);

            // Set priorNumber reading for next iteration
            prior = reading;
        }

        // Add all calculated readings and sort descending
        readings.addAll(calcReadings);
        Collections.sort(readings, Collections.reverseOrder());
    }

    private float getFeeForMonth(Calendar month) {

        for (final Tariff tariff : tariffs)
            if (tariff.hasFee() && tariff.isValidFeeFor(month))
                return tariff.getFee();
        return 0;
    }

    private Reading getInterpolation(Reading current, Reading prior) {

        // Get reading dates
        Calendar currentDate = current.getDate();
        Calendar priorDate = prior.getDate();

        // Get end of last month
        Calendar lastMonth = DateHelper.getLastMonth(currentDate);

        // Prior reading was at end of last month
        if (lastMonth.equals(priorDate))
            return prior;

        // Calculate total days between readings
        int daysTotal = DateHelper.getDaysBetween(priorDate, currentDate);

        // Calculate days between current reading and end of month
        int daysToLast = DateHelper.getDaysBetween(lastMonth, currentDate);

        // Calculate interpolated meter count
        float usageTotal = current.getCount() - prior.getCount();
        float usagePerDay = usageTotal / daysTotal;
        float newCounter = current.getCount() - usagePerDay * daysToLast;

        // Create new interpolated reading
        Reading reading = new Reading();
        reading.setMeter(this);
        reading.setDate(lastMonth);
        reading.setPrior(prior);
        reading.setCount((int) newCounter);
        reading.setCalculated(true);

        return reading;
    }

    private HashMap<String, Integer> getMonthlyConsumption() {

        return monthlyConsumption;
    }

    private float getPaymentForMonth(Calendar month) {

        for (final Tariff tariff : tariffs)
            if (tariff.hasPayment() && tariff.isValidPaymentFor(month))
                return tariff.getPayment();
        return 0;
    }

    private void updateConsumption() {

        monthlyConsumption = new HashMap<>();

        for (final Reading reading : readings) {
            reading.setPriceFrom(tariffs);
            String month = reading.getMonth();
            int consumption = reading.getConsumption();

            if (monthlyConsumption.containsKey(month))
                consumption += monthlyConsumption.get(month);
            monthlyConsumption.put(month, consumption);
        }
    }

    private void updateTariffValidity() {

        Calendar validToFee = DateHelper.getToday();
        Calendar validToPrice = DateHelper.getToday();
        Calendar validToPayment = DateHelper.getToday();

        Collections.sort(tariffs, Collections.reverseOrder());

        for (final Tariff tariff : tariffs) {

            if (tariff.hasFee()) {
                tariff.setValidToFee((Calendar) validToFee.clone());
                validToFee = tariff.getValidFrom();
                validToFee.add(Calendar.DAY_OF_MONTH, -1);
            }

            if (tariff.hasPrice()) {
                tariff.setValidToPrice((Calendar) validToPrice.clone());
                validToPrice = tariff.getValidFrom();
                validToPrice.add(Calendar.DAY_OF_MONTH, -1);
            }

            if (tariff.hasPayment()) {
                tariff.setValidToPayment((Calendar) validToPayment.clone());
                validToPayment = tariff.getValidFrom();
                validToPayment.add(Calendar.DAY_OF_MONTH, -1);
            }
        }
    }

    public enum Unit {
        kWh, m3 {
            @Override
            public String toString() {

                return "m³";
            }
        }
    }
}