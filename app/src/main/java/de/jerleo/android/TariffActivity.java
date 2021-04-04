package de.jerleo.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import de.jerleo.model.Meter;
import de.jerleo.model.Tariff;

public class TariffActivity extends Activity implements OnClickListener,
        OnCheckedChangeListener, OnDateChangedListener {

    private final NumberFormat nf = DecimalFormat.getNumberInstance();

    private Meter meter;
    private Tariff tariff;

    private DatePicker datePicker;
    private TextView unitPrice;
    private TextView monthlyFee;
    private TextView monthlyPayment;

    private CheckBox hasFee;
    private CheckBox hasPrice;
    private CheckBox hasPayment;

    private Button save;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.has_fee:
                monthlyFee.setEnabled(isChecked);
                break;
            case R.id.has_price:
                unitPrice.setEnabled(isChecked);
                break;
            case R.id.has_payment:
                monthlyPayment.setEnabled(isChecked);
                break;
        }
        save.setEnabled(hasFee.isChecked() || hasPrice.isChecked() || hasPayment.isChecked());
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.save:
                save();
                break;
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

        Calendar newDate = DateHelper.getToday();
        newDate.set(year, monthOfYear, dayOfMonth);
        tariff.setValidFrom(newDate);
    }

    private void save() {

        if (tariff == null) {
            tariff = new Tariff();
            meter.add(tariff);
        }

        String floatStr;

        float fee = 0;
        if (hasFee.isChecked() && monthlyFee.getText().length() > 0) {
            floatStr = monthlyFee.getText().toString();
            floatStr = floatStr.replace(",", ".");
            fee = Float.parseFloat(floatStr);
        }

        float price = 0;
        if (hasPrice.isChecked() && unitPrice.getText().length() > 0) {
            floatStr = unitPrice.getText().toString();
            floatStr = floatStr.replace(",", ".");
            price = Float.parseFloat(floatStr);
        }

        float payment = 0;
        if (hasPayment.isChecked() && monthlyPayment.getText().length() > 0) {
            floatStr = monthlyPayment.getText().toString();
            floatStr = floatStr.replace(",", ".");
            payment = Float.parseFloat(floatStr);
        }

        Calendar from = DateHelper.getDate(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth());

        tariff.setValidFrom(from);
        tariff.setFee(fee);
        tariff.setPrice(price);
        tariff.setPayment(payment);
        tariff.persist();

        meter.update();

        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        nf.setMaximumFractionDigits(5);
        nf.setMinimumFractionDigits(2);

        setContentView(R.layout.tariff);
        setTitle(R.string.tariff);

        datePicker = findViewById(R.id.valid_from);

        unitPrice = findViewById(R.id.unit_price);
        monthlyFee = findViewById(R.id.monthly_fee);
        monthlyPayment = findViewById(R.id.payment);

        hasFee = findViewById(R.id.has_fee);
        hasPrice = findViewById(R.id.has_price);
        hasPayment = findViewById(R.id.has_payment);

        save = findViewById(R.id.save);
        Button cancel = findViewById(R.id.cancel);

        final Bundle bundle = this.getIntent().getExtras();
        final int meterPosition = bundle.getInt("meter");
        meter = MainActivity.getHome().getMeter(meterPosition);

        if (bundle.containsKey("tariff")) {
            final int tariffPosition = bundle.getInt("tariff");
            tariff = meter.getTariff(tariffPosition);

            final Calendar date = tariff.getValidFrom();
            final int year = date.get(Calendar.YEAR);
            final int month = date.get(Calendar.MONTH);
            final int day = date.get(Calendar.DAY_OF_MONTH);

            datePicker.init(year, month, day, this);

            if (tariff.hasFee()) {
                hasFee.setChecked(true);
                monthlyFee.setEnabled(true);
                monthlyFee.setText(nf.format(tariff.getFee()));
            }

            if (tariff.hasPrice()) {
                hasPrice.setChecked(true);
                unitPrice.setEnabled(true);
                unitPrice.setText(nf.format(tariff.getPrice()));
            }

            if (tariff.hasPayment()) {
                hasPayment.setChecked(true);
                monthlyPayment.setEnabled(true);
                monthlyPayment.setText(nf.format(tariff.getPayment()));
            }
        }

        save.setOnClickListener(this);
        cancel.setOnClickListener(this);

        hasFee.setOnCheckedChangeListener(this);
        hasPrice.setOnCheckedChangeListener(this);
        hasPayment.setOnCheckedChangeListener(this);
    }
}
