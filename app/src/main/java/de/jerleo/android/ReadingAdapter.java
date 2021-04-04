package de.jerleo.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.jerleo.model.Reading;

class ReadingAdapter extends ArrayAdapter<Reading> {

    private final Context context;
    private final List<Reading> readings;

    @SuppressWarnings("SameParameterValue")
    public ReadingAdapter(Context context, int textViewResourceId, List<Reading> readings) {

        super(context, textViewResourceId, readings);
        this.context = context;
        this.readings = readings;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Reading reading = readings.get(position);

        View row = convertView;
        if (row == null) {
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.reading_row, parent, false);
            final ViewHolder holder = new ViewHolder();

            holder.date = row.findViewById(R.id.reading_date);
            holder.count = row.findViewById(R.id.reading_count);
            holder.costs = row.findViewById(R.id.reading_costs);
            holder.consumption = row.findViewById(R.id.consumption);
            row.setTag(holder);
        }

        final String currFmt = MainActivity.getCurrencyFormat();
        final String unitFmt = "%,d %s";
        final String unit = reading.getMeter().getUnit().toString();

        final ViewHolder holder = (ViewHolder) row.getTag();
        holder.date.setText(DateHelper.getDateString(reading.getDate()));
        holder.count.setText(String.format(unitFmt, reading.getCount(), unit));
        holder.consumption.setText(String.format(unitFmt, reading.getConsumption(), unit));
        holder.costs.setText(String.format(currFmt, reading.getCosts()));

        if (reading.isCalculated())
            row.setAlpha(0.5f);
        else
            row.setAlpha(1.0f);

        return row;
    }

    private static class ViewHolder {

        public TextView consumption;
        public TextView costs;
        public TextView count;
        public TextView date;
    }
}