package de.jerleo.android;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.jerleo.model.Bill;
import de.jerleo.model.BillItem;
import de.jerleo.model.Home;
import de.jerleo.model.Meter;

public class BillActivity extends FragmentActivity implements OnClickListener,
        DatePicker.OnDateChangedListener {

    private final NumberFormat nf = DecimalFormat.getNumberInstance();

    private Home home;
    private Bill bill = null;
    private Calendar begin = DateHelper.getToday();

    private int year;
    private int month;
    private int day;

    private TextView description;
    private TableLayout meterList;

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
    public void onDateChanged(DatePicker datePicker, int year, int month, int day) {

        this.year = year;
        this.month = month;
        this.day = day;
    }

    private List<BillItem> getBillItems() {

        final ArrayList<BillItem> items = new ArrayList<>();
        for (int idx = 0; idx < meterList.getChildCount(); ++idx) {
            final CheckBox checkBox = (CheckBox) meterList
                    .getChildAt(idx);
            if (checkBox.isChecked()) {
                final BillItem item = new BillItem();
                item.setMeter(checkBox.getTag().toString());
                items.add(item);
            }
        }
        return items;
    }

    private void save() {

        if (bill == null) {
            bill = new Bill();
            home.add(bill);
        }

        bill.setDescription(description.getText().toString().trim());
        bill.setBegin(DateHelper.getDate(year, month, day));
        bill.setItems(getBillItems());
        bill.persist();

        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill);
        setTitle(R.string.bill);

        nf.setMaximumFractionDigits(5);
        nf.setMinimumFractionDigits(2);

        description = findViewById(R.id.description);
        DatePicker datePicker = findViewById(R.id.begin);
        meterList = findViewById(R.id.meter_list);

        Button save = findViewById(R.id.save);
        Button cancel = findViewById(R.id.cancel);

        save.setOnClickListener(this);
        cancel.setOnClickListener(this);

        home = MainActivity.getHome();

        final Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            final int position = bundle.getInt("bill");
            bill = home.getBill(position);
            description.setText(bill.getDescription());
            begin = bill.getBegin();
        }

        year = begin.get(Calendar.YEAR);
        month = begin.get(Calendar.MONTH);
        day = begin.get(Calendar.DAY_OF_MONTH);

        datePicker.init(year, month, day, this);

        for (final Meter meter : home.getMeters()) {
            final CheckBox meterItem = new CheckBox(this);
            meterItem.setText(String.format("%s (%s)", meter.getName(), meter.getNumber()));
            meterItem.setTag(meter.getNumber());
            meterList.addView(meterItem);

            if (bill != null)
                meterItem.setChecked(bill.getMeters().contains(meter));
        }
    }
}
