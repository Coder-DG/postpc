package com.dginzbourg.postpc

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MessageDetailActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences
    private val db = FirebaseFirestore.getInstance()
    private lateinit var chatMessage: ChatMessage
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)
        sharedPref = this.getSharedPreferences(
                getString(R.string.shared_pref_chat_messages),
                Context.MODE_PRIVATE)
        val id = intent.getStringExtra(CHAT_MESSAGE_ID)
        deleteButton = findViewById(R.id.message_detail_permanent_delete_button)
        deleteButton.isEnabled = false
        db.collection(FIREBASE_CHAT_MESSAGES)
                .document(id)
                .get()
                .addOnSuccessListener { result ->
                    Log.d("firebase", "Got message ${result.id}.")
                    val tmpChatMessage = result.toObject(ChatMessage::class.java)
                    if (tmpChatMessage == null) {
                        setResult(Activity.RESULT_CANCELED)
                    } else {
                        chatMessage = tmpChatMessage
                        deleteButton.isEnabled = true
                        displayMessageDetails(chatMessage)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("firebase", "Error getting documents.", exception)
                    Thread {
                        val content = sharedPref.getString(CHAT_MESSAGE_CONTENT_PREFIX + id, null)
                        val timestampLong = sharedPref.getLong(CHAT_MESSAGE_TIMESTAMP_PREFIX + id, -1)
                        val timestamp = Timestamp(Date(timestampLong))
                        val phoneId = sharedPref.getString(CHAT_MESSAGE_PHONE_ID_PREFIX + id, "Unknown")
                        Log.i("restoreChatMessages", "Restored message $id.")
                        displayMessageDetails(ChatMessage(id, content ?: "", timestamp, phoneId
                                ?: "Unknown"))
                    }.start()
                }
        deleteButton.setOnClickListener {
            removeMessageFromDB(chatMessage)
        }

    }

    private fun displayMessageDetails(chatMessage: ChatMessage) = runOnUiThread {
        val text = "Sent on: ${chatMessage.timestamp.toDate()}\nFrom: ${chatMessage.phone_id}"
        findViewById<TextView>(R.id.message_detail_text).text = text
    }

    private fun removeMessageFromDB(chatMessage: ChatMessage) {
        removeMessageFromLocalDB(chatMessage)
        removeMessageFromRemoteDB(chatMessage)
    }

    private fun removeMessageFromLocalDB(chatMessage: ChatMessage) {
        with(sharedPref.edit()) {
            remove(CHAT_MESSAGE_CONTENT_PREFIX + chatMessage.id)
            remove(CHAT_MESSAGE_TIMESTAMP_PREFIX + chatMessage.id)
            apply()
        }
    }

    private fun removeMessageFromRemoteDB(chatMessage: ChatMessage) {
        db.collection(FIREBASE_CHAT_MESSAGES)
                .document(chatMessage.id)
                .delete()
                .addOnSuccessListener {
                    Log.d("firebase", "Successfully deleted chat message " +
                            "with ID: ${chatMessage.id}")
                }
    }
}
