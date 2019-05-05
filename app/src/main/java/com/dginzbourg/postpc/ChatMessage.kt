package com.dginzbourg.postpc

import com.google.firebase.Timestamp

data class ChatMessage(val id: String, val content: String, val timestamp: Timestamp)
