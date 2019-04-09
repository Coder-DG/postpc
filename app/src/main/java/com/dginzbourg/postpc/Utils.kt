package com.dginzbourg.postpc

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

internal object Utils {
    fun showToast(context: Context, message: String, duration: Int) {
        val toast = Toast.makeText(context, message, duration)
        toast.show()
    }

    fun getAlertDialog(
            activity: Activity,
            message: String,
            title: String,
            posButtonText: String,
            negButtonText: String,
            posFunction: () -> Unit,
            negFunction: () -> Unit = {}): AlertDialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(posButtonText) { _, _ -> posFunction() }
                .setNegativeButton(negButtonText) { _, _ -> negFunction() }
        return builder.create()
    }
}
