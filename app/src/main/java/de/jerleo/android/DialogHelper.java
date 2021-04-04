package de.jerleo.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

class DialogHelper implements
        android.content.DialogInterface.OnClickListener {

    public AlertDialog getAlertDialog(Context ctx, int titleId, int messageId) {

        final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
        dialog.setTitle(titleId);
        dialog.setMessage(ctx.getString(messageId));
        dialog.setButton(BUTTON_POSITIVE, ctx.getString(R.string.ok), this);
        dialog.setButton(BUTTON_NEGATIVE, ctx.getString(R.string.cancel), this);
        return dialog;
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
        }
    }

    public interface DialogCommand {

        void execute();
    }
}
