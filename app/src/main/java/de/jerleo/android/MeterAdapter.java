package de.jerleo.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import de.jerleo.model.Meter;
import de.jerleo.model.Reading;

class MeterAdapter extends ArrayAdapter<Meter> {

    private final Context context;
    private final DecimalFormat df = new DecimalFormat(",###");
    private final MeterList meterList;
    private final List<Meter> meters;

    @SuppressWarnings("SameParameterValue")
    public MeterAdapter(Context context, int textViewResourceId,
                        List<Meter> meters, MeterList meterList) {

        super(context, textViewResourceId, meters);
        this.meterList = meterList;
        this.context = context;
        this.meters = meters;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        if (row == null) {
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.meter_row, parent, false);
            final ViewHolder holder = new ViewHolder();

            holder.name = row.findViewById(R.id.meter_name);
            holder.number = row.findViewById(R.id.meter_number);
            holder.date = row.findViewById(R.id.meter_date);
            holder.count = row.findViewById(R.id.meter_count);

            holder.chart = row.findViewById(R.id.chart_button);
            holder.chart.setOnClickListener(v -> {

                final Integer position1 = (Integer) v.getTag();
                meterList.showChart(position1);
            });

            holder.more = row.findViewById(R.id.more_button);
            holder.more.setOnClickListener(v -> {

                final Integer position12 = (Integer) v.getTag();
                meterList.showReadings(position12);
            });
            row.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) row.getTag();

        final Meter meter = meters.get(position);
        final Reading reading = meter.getLastReading();
        final String unit = " " + meter.getUnit().toString();

        String dateStr = "";
        String countStr = "";

        if (reading != null) {
            dateStr = DateHelper.getDateString(reading.getDate());
            countStr = df.format(reading.getCount()) + unit;
        }

        holder.number.setText(meter.getNumber());
        holder.name.setText(meter.getName());
        holder.date.setText(dateStr);
        holder.count.setText(countStr);

        holder.chart.setTag(position);
        holder.more.setTag(position);

        return row;
    }

    static class ViewHolder {

        public TextView name;
        public TextView number;
        public TextView count;
        public TextView date;
        public ImageButton chart;
        public ImageButton more;
    }

}
