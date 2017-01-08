package de.jerleo.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.jerleo.model.Meter;
import de.jerleo.model.Reading;

public class ReadingActivity extends FragmentActivity implements OnClickListener,
        OnDateChangedListener {

    private final ArrayList<TextView> digits = new ArrayList<>();
    private final NumberFormat nf = DecimalFormat.getNumberInstance();

    private String formatStr = "";
    private Reading reading;
    private Meter meter;
    private Calendar minDate = null;

    private DatePicker readingDate;
    private TextView meterUnit;
    private TextView readingCosts;
    private Button save;

    private View minus1;
    private View minus10;
    private View minus100;
    private View minus1000;

    private int count;
    private int lastCount;
    private int viewCount;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.plus_1000:
                count += 1000;
                break;
            case R.id.plus_100:
                count += 100;
                break;
            case R.id.plus_10:
                count += 10;
                break;
            case R.id.plus_1:
                count += 1;
                break;
            case R.id.minus_1000:
                count -= 1000;
                break;
            case R.id.minus_100:
                count -= 100;
                break;
            case R.id.minus_10:
                count -= 10;
                break;
            case R.id.minus_1:
                count -= 1;
                break;
            case R.id.reading_save:
                save.setEnabled(false);
                meter.add(reading);
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }

        reading.setCount(count);

        if (viewCount != count) {
            updateViews();
            updateCosts();
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

        Calendar newDate = DateHelper.getToday();
        newDate.set(year, monthOfYear, dayOfMonth);
        reading.setDate(newDate);
        updateCosts();
    }

    private void assignDigits() {

        final int[] digit_ids = {R.id.digit_0, R.id.digit_1,
                R.id.digit_2, R.id.digit_3, R.id.digit_4,
                R.id.digit_5, R.id.digit_6, R.id.digit_7};

        for (int digit_id : digit_ids) {
            final TextView digit = (TextView) findViewById(digit_id);
            digits.add(digit);
            formatStr += "0";
        }
    }

    private void findViews() {

        readingDate = (DatePicker) findViewById(R.id.reading_date);
        readingCosts = (TextView) findViewById(R.id.reading_costs);
        meterUnit = (TextView) findViewById(R.id.meter_unit);
        save = (Button) findViewById(R.id.reading_save);

        minus1000 = findViewById(R.id.minus_1000);
        minus100 = findViewById(R.id.minus_100);
        minus10 = findViewById(R.id.minus_10);
        minus1 = findViewById(R.id.minus_1);
    }

    private void getReading() {

        final Bundle bundle = this.getIntent().getExtras();
        final int position = bundle.getInt("meter");
        meter = MainActivity.getHome().getMeter(position);
        meterUnit.setText(meter.getUnit().toString());

        reading = new Reading();
        Reading lastReading = meter.getLastReading();

        if (lastReading != null) {
            lastCount = count = lastReading.getCount();

            // New reading must be after last reading
            minDate = lastReading.getDate();
            minDate.add(Calendar.DAY_OF_MONTH, 1);

            minDate.set(Calendar.HOUR_OF_DAY, minDate.getActualMaximum(Calendar.HOUR_OF_DAY));
            minDate.set(Calendar.MINUTE, minDate.getActualMaximum(Calendar.MINUTE));
            minDate.set(Calendar.SECOND, minDate.getActualMaximum(Calendar.SECOND));

            reading.setCount(count);
            reading.setPrior(lastReading);
        }
    }

    private void setButtonListeners() {

        final int[] buttons = {R.id.plus_1000, R.id.plus_100, R.id.plus_10,
                R.id.plus_1, R.id.minus_1000, R.id.minus_100, R.id.minus_10,
                R.id.minus_1, R.id.reading_save, R.id.cancel};

        for (final int button : buttons) {
            final View view = findViewById(button);
            view.setOnClickListener(this);
        }
    }

    private void setupDateListener() {

        final Calendar date = DateHelper.getToday();
        final int year = date.get(Calendar.YEAR);
        final int month = date.get(Calendar.MONTH);
        final int day = date.get(Calendar.DAY_OF_MONTH);

        reading.setDate(date);

        readingDate.setMinDate(minDate == null ? 0 : minDate.getTimeInMillis());
        readingDate.setMaxDate(date.getTimeInMillis());
        readingDate.init(year, month, day, this);
    }

    private void updateCosts() {

        reading.setPriceFrom(meter.getTariffs());
        readingCosts.setText(String.format(MainActivity.getCurrencyFormat(), reading.getCosts()));
    }

    private void updateViews() {

        final char[] number = new DecimalFormat(formatStr).format(count).toCharArray();

        int digit = 0;
        for (TextView textView : digits) textView.setText(number, digit++, 1);

        final View ok = findViewById(R.id.reading_save);
        ok.setEnabled(count > lastCount && readingDate.getMinDate() <= readingDate.getMaxDate());

        minus1000.setEnabled(count - 1000 >= lastCount);
        minus100.setEnabled(count - 100 >= lastCount);
        minus10.setEnabled(count - 10 >= lastCount);
        minus1.setEnabled(count - 1 >= lastCount);

        viewCount = count;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        setContentView(R.layout.reading);

        findViews();
        getReading();

        setTitle(getString(R.string.new_reading) + ": " + meter.getNumber());

        setButtonListeners();
        setupDateListener();

        assignDigits();
        updateViews();
        updateCosts();
    }
}