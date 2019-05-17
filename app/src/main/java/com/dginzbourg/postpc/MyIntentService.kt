package com.dginzbourg.postpc

import android.app.IntentService
import android.content.Intent
import android.util.Log

class MyIntentService: IntentService("MyIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        Log.d("INTENT_SERVICE", "Inside service")
    }

}