package de.jerleo.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.jerleo.model.Meter;

public class ReadingChart extends Activity {

    private Meter meter;

    private LineData getAverageData() {

        List<Entry> average = new ArrayList<>();

        for (int month = 0; month < 12; month++) {

            @SuppressWarnings("UnnecessaryLocalVariable")
            float x = month;

            @SuppressLint("DefaultLocale")
            float y = meter.getAverageFor(String.format("%1$02d", month));

            average.add(new Entry(x, y));
        }

        LineData lineData = new LineData();
        LineDataSet lineDataSet = new LineDataSet(average, getString(R.string.average));
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setColor(Color.YELLOW);
        lineData.addDataSet(lineDataSet);
        lineData.setDrawValues(false);

        return lineData;
    }

    private BarData getCurrentData() {

        Calendar calendar = meter.getLastReading().getDate();
        int year = calendar.get(Calendar.YEAR);

        List<BarEntry> currentYear = new ArrayList<>();
        List<BarEntry> priorYear = new ArrayList<>();

        for (int month = 0; month < 12; month++) {
            float x = calendar.get(Calendar.MONTH);
            float y = meter.getConsumptionFor(calendar);

            if (calendar.get(Calendar.YEAR) == year)
                currentYear.add(new BarEntry(x, y));
            else
                priorYear.add(new BarEntry(x, y));

            calendar.add(Calendar.MONTH, -1);
        }

        BarDataSet currentData = new BarDataSet(currentYear, String.valueOf(year));
        currentData.setColors(new int[]{R.color.chartCurrent}, this);

        BarDataSet priorData = new BarDataSet(priorYear, String.valueOf(year - 1));
        priorData.setColors(new int[]{R.color.chartPrior}, this);

        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(currentData);
        dataSets.add(priorData);

        BarData barData = new BarData(dataSets);
        barData.setDrawValues(false);
        barData.setBarWidth(0.45f);
        return barData;
    }

    public class MonthAxisFormatter implements IAxisValueFormatter {

        private final Locale locale = Locale.getDefault();
        private final Calendar calendar = Calendar.getInstance();

        @Override
        public String getFormattedValue(float value, AxisBase axis) {

            calendar.set(Calendar.MONTH, (int) value);
            return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reading_chart);

        final Bundle bundle = this.getIntent().getExtras();

        int meterPosition = bundle.getInt("meter");
        meter = MainActivity.getHome().getMeter(meterPosition);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(getAverageData());
        combinedData.setData(getCurrentData());

        CombinedChart chart = (CombinedChart) findViewById(R.id.reading_chart);
        chart.setData(combinedData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new MonthAxisFormatter());
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(11.5f);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(12);
        xAxis.setTextColor(Color.GRAY);

        chart.getAxisLeft().setTextColor(Color.GRAY);
        chart.getAxisRight().setTextColor(Color.GRAY);
        chart.getLegend().setTextColor(Color.WHITE);

        chart.getDescription().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.animateY(1500);
    }

}
