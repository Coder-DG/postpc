package com.dginzbourg.postpc

import com.google.firebase.Timestamp
import java.io.Serializable

data class ChatMessage(val id: String = "",
                       val content: String = "",
                       val timestamp: Timestamp = Timestamp.now()) : Serializable
