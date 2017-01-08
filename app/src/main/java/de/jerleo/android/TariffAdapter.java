package de.jerleo.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import de.jerleo.model.Tariff;

class TariffAdapter extends ArrayAdapter<Tariff> {

    private final Context context;
    private final NumberFormat nf = DecimalFormat.getNumberInstance();
    private final List<Tariff> tariffs;

    @SuppressWarnings("SameParameterValue")
    public TariffAdapter(Context context, int textViewResourceId,
                         List<Tariff> tariffs) {

        super(context, textViewResourceId, tariffs);
        this.context = context;
        this.tariffs = tariffs;

        nf.setMaximumFractionDigits(5);
        nf.setMinimumFractionDigits(2);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        if (row == null) {
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.tariff_row, parent, false);
            final ViewHolder holder = new ViewHolder();

            holder.begin = (TextView) row.findViewById(R.id.valid_from);
            holder.fee = (TextView) row.findViewById(R.id.monthly_fee);
            holder.price = (TextView) row.findViewById(R.id.unit_price);
            holder.payment = (TextView) row.findViewById(R.id.payment);

            row.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) row.getTag();
        final Tariff tariff = tariffs.get(position);
        final String meterUnit = tariff.getMeter().getUnit().toString();
        final String currFmt = MainActivity.getCurrencyFormat();
        final String currency = MainActivity.getCurrencySymbol();

        holder.begin.setText(DateHelper.getDateString(tariff.getValidFrom()));

        final float unitPrice = tariff.getPrice();
        final float monthlyFee = tariff.getFee();
        final float payment = tariff.getPayment();

        String priceStr = "";
        String feeStr = "";
        String payStr = "";

        if (unitPrice > 0)
            priceStr = nf.format(unitPrice) + " " + currency + "/" + meterUnit;

        if (monthlyFee > 0)
            feeStr = String.format(currFmt, monthlyFee);

        if (payment > 0)
            payStr = String.format(currFmt, payment);

        holder.fee.setText(feeStr);
        holder.price.setText(priceStr);
        holder.payment.setText(payStr);

        return row;
    }

    static class ViewHolder {

        public TextView begin;
        public TextView fee;
        public TextView price;
        public TextView payment;
    }
}