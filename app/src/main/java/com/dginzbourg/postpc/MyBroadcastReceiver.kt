package com.dginzbourg.postpc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log

private const val TAG = "broadcast_receiver"
class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            Log.d(TAG, "Intent is null, returning.")
            return
        }
        Log.d(TAG, "Fetching Settings data...")
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val sendTo = sharedPreferences.getString(PHONE_NO_KEY, "") ?: ""
        val smsPrefix = sharedPreferences.getString(SMS_PREFIX, "") ?: ""
        Log.d(TAG, "Fetched Settings data.")
        if (sendTo.isEmpty() || smsPrefix.isEmpty())
            return

        val calledTo = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        val smsManager = SmsManager.getDefault()
        Log.d(TAG, "Sending an SMS...")
        smsManager.sendTextMessage(sendTo, sendTo, smsPrefix + calledTo, null, null)
        Log.d(TAG, "SMS sent to $sendTo, from $sendTo")
    }
}