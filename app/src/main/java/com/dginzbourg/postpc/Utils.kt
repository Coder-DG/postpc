package com.dginzbourg.postpc

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import kotlin.random.Random

internal object Utils {
    private const val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
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

    fun generateMessageId(): String {
        var messageId = ""
        for (i in 0..30) {
            messageId += chars[Random.nextInt(0, chars.length)]
        }
        return messageId
    }
}

internal const val SINGLE_CHAT_MESSAGE = "single_chat_message"
internal const val FIREBASE_CHAT_MESSAGES = "chat_messages"
internal const val FIREBASE_DEFAULTS = "defaults"
internal const val FIREBASE_DEFAULTS_USERNAME_DOC_ID = "username"
internal const val FIREBASE_DEFAULTS_USERNAME_NAME_KEY = "name"
