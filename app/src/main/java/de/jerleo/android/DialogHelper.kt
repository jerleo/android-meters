package de.jerleo.android

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

internal class DialogHelper : DialogInterface.OnClickListener {

    interface DialogCommand {
        fun execute()
    }

    interface OnListChangedListener {
        fun onListChanged()
    }

    fun getAlertDialog(ctx: Context?, titleId: Int, messageId: Int): AlertDialog =
        AlertDialog.Builder(ctx).create().apply {
            this.setTitle(titleId)
            this.setMessage(ctx?.getString(messageId))
            this.setButton(
                DialogInterface.BUTTON_POSITIVE,
                ctx?.getString(R.string.ok),
                this@DialogHelper
            )
            this.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                ctx?.getString(R.string.cancel),
                this@DialogHelper
            )
        }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val ok = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
                val command = ok.tag as DialogCommand
                command.execute()
            }
            DialogInterface.BUTTON_NEGATIVE -> dialog.cancel()
        }
    }
}