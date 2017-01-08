package de.jerleo.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.jerleo.model.Meter;

public class MeterActivity extends FragmentActivity implements OnClickListener {

    private Meter meter = null;
    private TextView name;
    private TextView number;

    private Button save;
    private Spinner unit;
    private Spinner prior;

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

    private ArrayList<String> getPriorMetersFor(Meter meter) {

        ArrayList<String> result = new ArrayList<>();
        result.add(getString(R.string.none));

        List<Meter> meters = MainActivity.getHome().getMeters();
        for (Meter other : meters)
            if (other.getId() != meter.getId())
                if (other.getUnit().equals(meter.getUnit()))
                    result.add(other.getNumber());
        return result;
    }

    private TextWatcher getTextWatcher() {

        return new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                final String entry = s.toString().trim();
                save.setEnabled(entry.length() > 0);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        };
    }

    private void save() {

        if (meter == null) {
            meter = new Meter();
            MainActivity.getHome().add(meter);
        }

        meter.setNumber(number.getText().toString().trim());
        meter.setName(name.getText().toString().trim());
        meter.setUnit(Meter.Unit.values()[unit.getSelectedItemPosition()]);
        meter.setPrior((String) prior.getSelectedItem());
        meter.persist();

        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.meter);
        setTitle(R.string.meter);

        number = (TextView) findViewById(R.id.meter_number);
        name = (TextView) findViewById(R.id.meter_name);
        unit = (Spinner) findViewById(R.id.meter_unit);
        prior = (Spinner) findViewById(R.id.prior_meter);
        save = (Button) findViewById(R.id.save);
        Button cancel = (Button) findViewById(R.id.cancel);

        boolean update = false;

        final Bundle bundle = this.getIntent().getExtras();

        if (bundle != null) {

            final int position = bundle.getInt("meter");
            meter = MainActivity.getHome().getMeter(position);
            number.setText(meter.getNumber());
            name.setText(meter.getName());
            unit.setSelection(meter.getUnit().ordinal());

            ArrayList<String> priorMeters = getPriorMetersFor(meter);
            prior.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    priorMeters));
            if (meter.hasPrior())
                prior.setSelection(priorMeters.indexOf(meter.getPrior().getNumber()));

            update = true;
        }

        number.addTextChangedListener(getTextWatcher());
        cancel.setOnClickListener(this);
        save.setOnClickListener(this);
        save.setEnabled(update);

        unit.setEnabled(!update);
        prior.setEnabled(update);
    }
}