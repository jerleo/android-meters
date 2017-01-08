package de.jerleo.android;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.jerleo.model.Bill;

class BillAdapter extends ArrayAdapter<Bill> {

    private final List<Bill> bills;
    private final Context context;

    @SuppressWarnings("ResourceType")
    public BillAdapter(Context context, List<Bill> bills) {

        super(context, R.id.bill_row, bills);
        this.context = context;
        this.bills = bills;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        if (row == null) {
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.bill_row, parent, false);
            final ViewHolder holder = new ViewHolder();

            holder.description = (TextView) row.findViewById(R.id.description);
            holder.begin = (TextView) row.findViewById(R.id.begin);
            holder.end = (TextView) row.findViewById(R.id.end);
            holder.fees = (TextView) row.findViewById(R.id.fees);
            holder.costs = (TextView) row.findViewById(R.id.costs);
            holder.payments = (TextView) row.findViewById(R.id.payments);
            holder.balance = (TextView) row.findViewById(R.id.balance);
            holder.textColor = holder.balance.getCurrentTextColor();

            row.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) row.getTag();
        final Bill bill = bills.get(position);

        final String currFmt = MainActivity.getCurrencyFormat();

        final float fees = bill.getTotalFees();
        final float costs = bill.getTotalCosts();
        final float payments = bill.getTotalPayments();
        final float balance = payments - fees - costs;

        holder.description.setText(bill.getDescription());
        holder.begin.setText(DateHelper.getDateString(bill.getBegin()));
        holder.end.setText(DateHelper.getDateString(bill.getEnd()));

        holder.fees.setText(String.format(currFmt, fees));
        holder.costs.setText(String.format(currFmt, costs));
        holder.payments.setText(String.format(currFmt, payments));
        holder.balance.setText(String.format(currFmt, balance));

        if (balance < 0)
            holder.balance.setTextColor(Color.RED);
        else
            holder.balance.setTextColor(holder.textColor);

        return row;

    }

    static class ViewHolder {

        public TextView description;
        public TextView begin;
        public TextView end;
        public TextView fees;
        public TextView costs;
        public TextView payments;
        public TextView balance;
        public int textColor;
    }
}
