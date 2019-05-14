package com.dginzbourg.postpc

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MessageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)
        val id = intent.getStringExtra(CHAT_MESSAGE_ID)
        FirebaseFirestore.getInstance()
                .collection(FIREBASE_CHAT_MESSAGES)
                .document(id)
                .get()
                .addOnSuccessListener { result ->
                    Log.d("firebase", "Got message ${result.id}.")
                    val chatMessage = result.toObject(ChatMessage::class.java)
                    if (chatMessage == null)
                        setResult(Activity.RESULT_CANCELED)
                    else
                        displayMessageDetails(chatMessage)
                }
                .addOnFailureListener { exception ->
                    Log.w("firebase", "Error getting documents.", exception)
                    Thread {
                        val sharedPref = this.getSharedPreferences(
                                getString(R.string.shared_pref_chat_messages),
                                Context.MODE_PRIVATE)
                        val content = sharedPref.getString(CHAT_MESSAGE_CONTENT_PREFIX + id, null)
                        val timestampLong = sharedPref.getLong(CHAT_MESSAGE_TIMESTAMP_PREFIX + id, -1)
                        val timestamp = Timestamp(Date(timestampLong))
                        val phoneId = sharedPref.getString(CHAT_MESSAGE_PHONE_ID_PREFIX + id, "Unknown")
                        Log.i("restoreChatMessages", "Restored message $id.")
                        displayMessageDetails(ChatMessage(id, content ?: "", timestamp, phoneId
                                ?: "Unknown"))
                    }.start()
                }
    }

    private fun displayMessageDetails(chatMessage: ChatMessage) = runOnUiThread {
        val text = "Sent on: ${chatMessage.timestamp}\nFrom: ${chatMessage.phone_id}"
        findViewById<TextView>(R.id.message_detail_text).text = text
    }

//    fun removeChatMessageAt(pos: Int) {
//        val chatMessagesCopy = ArrayList(chatMessages)
//        val chatMessage = chatMessagesCopy.removeAt(pos)
//        messageIDs.remove(chatMessage.id)
//        removeMessageFromDB(chatMessage)
//        chatMessages = chatMessagesCopy
//        adapter.submitList(chatMessages)
//    }
}
