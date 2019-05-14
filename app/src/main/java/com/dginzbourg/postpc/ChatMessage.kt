package com.dginzbourg.postpc

import android.os.Build
import com.google.firebase.Timestamp

data class ChatMessage(val id: String = "",
                       val content: String = "",
                       val timestamp: Timestamp = Timestamp.now(),
                       val phone_id: String = Build.MANUFACTURER + " " + Build.MODEL)
