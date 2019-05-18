package com.dginzbourg.postpc

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
private const val TAG = "INTENT_SERVICE"
class MyIntentService : IntentService("MyIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "Inside service")
        val context = applicationContext
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val lastUsedId = sharedPreferences.getInt(LAST_USED_ID_KEY, 1000)

        Log.d(TAG, "Notifying with ${intent?.getStringExtra(MESSAGE_KEY)}")
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(intent?.getStringExtra(MESSAGE_KEY) ?: "THIS SHOULDN'T BE REACHED")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(lastUsedId + 1, builder.build())
        }
        with(sharedPreferences.edit()) {
            putInt(LAST_USED_ID_KEY, lastUsedId + 1)
            apply()
        }
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
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}