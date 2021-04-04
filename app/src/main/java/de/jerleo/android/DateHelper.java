package de.jerleo.android;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DateHelper {

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat database = new SimpleDateFormat("yyyy-MM-dd");

    private static DateFormat dateFormat;

    @Nullable
    public static Calendar getDatabaseDate(String dateString) {

        final Calendar calendar = getToday();
        try {
            calendar.setTime(database.parse(dateString));
        } catch (final ParseException e) {
            return null;
        }
        return calendar;
    }

    public static String getDatabaseDate(Calendar date) {

        return database.format(date.getTime());
    }

    public static Calendar getDate(int year, int month, int day) {

        final Calendar result = getToday();
        result.set(Calendar.YEAR, year);
        result.set(Calendar.MONTH, month);
        result.set(Calendar.DAY_OF_MONTH, day);
        return result;
    }

    public static String getDateString(Calendar date) {

        return dateFormat.format(date.getTime());
    }

    public static int getDaysBetween(Calendar start, Calendar end) {

        return (int) TimeUnit.MILLISECONDS.toDays(
                Math.abs(end.getTimeInMillis() - start.getTimeInMillis()));
    }

    public static Calendar getLastMonth(Calendar date) {

        final Calendar last = (Calendar) date.clone();
        last.set(Calendar.DAY_OF_MONTH, 1);
        last.add(Calendar.DAY_OF_MONTH, -1);
        return last;
    }

    @SuppressLint("DefaultLocale")
    public static String getMonthKey(Calendar date) {

        return String.format("%1$04d/%2$02d", date.get(Calendar.YEAR), date.get(Calendar.MONTH));
    }

    public static Calendar getNow() {

        return Calendar.getInstance();
    }

    public static String getTimestamp(Calendar date) {

        return timestamp.format(date.getTime());
    }

    public static Calendar getToday() {

        final Calendar calendar = getNow();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        return calendar;
    }

    public static void setContext(Context context) {

        DateHelper.dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
    }
}