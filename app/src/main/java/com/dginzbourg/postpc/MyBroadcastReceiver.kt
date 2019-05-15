package com.dginzbourg.postpc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null)
            return

        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val sendTo = sharedPreferences.getString(PHONE_NO_KEY, "") ?: ""
        val smsPrefix = sharedPreferences.getString(SMS_PREFIX, "") ?: ""

        if (sendTo.isEmpty() || smsPrefix.isEmpty())
            return

        val calledTo = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(sendTo, sendTo, smsPrefix + calledTo, null, null)
    }
}