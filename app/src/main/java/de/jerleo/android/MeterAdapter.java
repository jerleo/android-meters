package de.jerleo.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

            holder.name = (TextView) row.findViewById(R.id.meter_name);
            holder.number = (TextView) row.findViewById(R.id.meter_number);
            holder.date = (TextView) row.findViewById(R.id.meter_date);
            holder.count = (TextView) row.findViewById(R.id.meter_count);

            holder.chart = (ImageButton) row.findViewById(R.id.chart_button);
            holder.chart.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    final Integer position = (Integer) v.getTag();
                    meterList.showChart(position);
                }
            });

            holder.more = (ImageButton) row.findViewById(R.id.more_button);
            holder.more.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    final Integer position = (Integer) v.getTag();
                    meterList.showReadings(position);
                }
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
