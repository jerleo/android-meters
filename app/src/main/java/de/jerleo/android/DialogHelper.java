package de.jerleo.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

class DialogHelper implements
        android.content.DialogInterface.OnClickListener {

    private String choice;
    private String choices[];

    public AlertDialog getAlertDialog(Context ctx, int titleId, int messageId) {

        final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
        dialog.setTitle(titleId);
        dialog.setMessage(ctx.getString(messageId));
        dialog.setButton(BUTTON_POSITIVE, ctx.getString(R.string.ok), this);
        dialog.setButton(BUTTON_NEGATIVE, ctx.getString(R.string.cancel), this);
        return dialog;
    }

    public String getChoice() {

        return choice;
    }

    @SuppressWarnings("SameParameterValue")
    public AlertDialog getChoiceDialog(Context ctx, int title, String[] choices) {

        this.choice = null;
        this.choices = choices;
        final ListAdapter adapter = new ArrayAdapter<>(ctx, R.layout.alert_item, choices);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getString(title));
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
        builder.setSingleChoiceItems(adapter, -1, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        switch (which) {
            case BUTTON_POSITIVE:
                final Button ok = ((AlertDialog) dialog).getButton(BUTTON_POSITIVE);
                final DialogCommand command = (DialogCommand) ok.getTag();
                command.execute();
                break;
            case BUTTON_NEGATIVE:
                dialog.cancel();
                break;
            default:
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                setChoice(DialogHelper.this.choices[which]);
                break;
        }
    }

    private void setChoice(String choice) {

        this.choice = choice;
    }

    public interface DialogCommand {

        void execute();
    }
}
