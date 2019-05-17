package com.dginzbourg.postpc

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.telephony.SmsManager
import android.util.Log

private const val TAG = "broadcast_receiver"
private const val NOTIFICATIONS_CHANNEL_NAME = "postpc_channel"
private const val CHANNEL_ID = "com.dginzbourg.postpc_notifications_channel"
private const val LAST_USED_ID = "last_used_ID"
private const val MESSAGE_KEY = "message"
private const val MESSAGE_NOTIFICATION_JOB_ID = 999

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("onReceive", "Called")
        if (intent == null || context == null) {
            Log.d(TAG, "Intent is null, returning.")
            return
        }

        createNotificationsChannel(context)

        Log.d(TAG, "Fetching Settings data...")
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val sendTo = sharedPreferences.getString(PHONE_NO_KEY, "") ?: ""
        val smsPrefix = sharedPreferences.getString(SMS_PREFIX, "") ?: ""
        val lastUsedId = sharedPreferences.getInt(LAST_USED_ID, 1000)
        Log.d(TAG, "Fetched Settings data.")
        if (sendTo.isEmpty() || smsPrefix.isEmpty())
            return

        val calledTo = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        val smsManager = SmsManager.getDefault()
        Log.d(TAG, "Sending an SMS...")
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("sending message...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(lastUsedId + 1, builder.build())
        }
        with(sharedPreferences.edit()) {
            putInt(LAST_USED_ID, lastUsedId + 1)
            apply()
        }
        val sentIntent = Intent(context, MyIntentService::class.java).apply {
            putExtra(MESSAGE_KEY, "message sent successfully!")
        }
        val deliveryIntent = Intent(context, MyIntentService::class.java).apply {
            putExtra(MESSAGE_KEY, "message received successfully!")
        }
        val sentPendingIntent = PendingIntent.getService(context, 0, sentIntent, 0)
        val deliveryPendingIntent = PendingIntent.getService(context, 0, sentIntent, 0)
        smsManager.sendTextMessage(sendTo, null, smsPrefix + calledTo, sentPendingIntent, deliveryPendingIntent)
        Log.d(TAG, "SMS sent to $sendTo")
    }

    private fun createNotificationsChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel(
                CHANNEL_ID,
                NOTIFICATIONS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}