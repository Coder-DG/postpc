package com.dginzbourg.postpc

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class MainActivity : AppCompatActivity() {


    private var adapter = MessageRecyclerUtils.ChatMessageAdapter(OnItemClickCallback())
    private var messageIDs: MutableSet<String> = HashSet()
    private var chatMessages = ArrayList<ChatMessage>()
    private lateinit var editText: EditText
    private var editTextString: String? = ""
    private val db = FirebaseFirestore.getInstance()
    private lateinit var sharedPref: SharedPreferences

    private fun getAvailableMessageID(): String {
        var id: String
        do {
            id = Utils.generateMessageId()
        } while (id in messageIDs)
        return id
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = this.getSharedPreferences(
                getString(R.string.shared_pref_chat_messages),
                Context.MODE_PRIVATE)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.edit_text)
        editText.setText(editTextString)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener { handleSend() }

        val recyclerView = findViewById<RecyclerView>(R.id.chat_messages_recycler)
        recyclerView.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false)
        recyclerView.adapter = adapter
    }

    private fun handleSend() {
        val text = editText.text.toString()
        if (text.isEmpty()) {
            Utils.showToast(
                    this@MainActivity,
                    getString(R.string.empty_message_error),
                    R.integer.toast_duration)
            return
        }
        addChatMessage(text)
        editText.setText("")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let { restoreTextViews(savedInstanceState) }
    }

    private fun restoreTextViews(savedInstanceState: Bundle) {
        editTextString = savedInstanceState.getString(EDIT_TEXT_STRING_KEY)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveTextViews(outState)
    }

    private fun saveTextViews(outState: Bundle) {
        outState.putString(EDIT_TEXT_STRING_KEY, editTextString)
    }


    inner class OnItemClickCallback : MessageRecyclerUtils.ChatMessageLongClickCallBack {
        override fun onLongClick(pos: Int): Boolean {
            Log.d("onLongClick", "Clicked on item $pos")
            val dialog = Utils.getAlertDialog(
                    this@MainActivity,
                    getString(R.string.deletion_alert),
                    getString(R.string.alert),
                    getString(R.string.yes),
                    getString(R.string.cancel),
                    { removeChatMessageAt(pos) })
            dialog?.show()
            return true
        }
    }

    fun removeChatMessageAt(pos: Int) {
        val chatMessagesCopy = ArrayList(chatMessages)
        val chatMessage = chatMessagesCopy.removeAt(pos)
        messageIDs.remove(chatMessage.id)
        removeMessageFromDB(chatMessage)
        chatMessages = chatMessagesCopy
        adapter.submitList(chatMessages)
    }

    private fun removeMessageFromDB(chatMessage: ChatMessage) {
        with(sharedPref.edit()) {
            remove(CHAT_MESSAGE_CONTENT_PREFIX + chatMessage.id)
            remove(CHAT_MESSAGE_TIMESTAMP_PREFIX + chatMessage.id)
            apply()
        }
    }

    private fun addChatMessage(content: String) {
        val chatMessagesCopy = ArrayList(chatMessages)
        val messageId = getAvailableMessageID()
        val chatMessage = ChatMessage(messageId, content, Timestamp(Date()))
        chatMessagesCopy.add(chatMessage)
        messageIDs.add(messageId)
        saveChatMessageToDB(chatMessage)
        Log.d("addChatMessage", "Inserted content: $content")
        chatMessages = chatMessagesCopy
        adapter.submitList(chatMessages)
    }

    override fun onResume() {
        super.onResume()
        restoreChatMessages()
    }

    override fun onPause() {
        super.onPause()
        saveMessagesIDs()
    }

    private fun saveMessagesIDs() {
        with(sharedPref.edit()) {
            putStringSet(CHAT_MESSAGES_IDS, messageIDs)
            apply()
        }
    }

    private fun saveChatMessageToDB(chatMessage: ChatMessage) {
        saveChatMessageToLocalDB(chatMessage)
        saveChatMessageToRemoteDB(chatMessage)
    }

    private fun saveChatMessageToLocalDB(chatMessage: ChatMessage) {
        with(sharedPref.edit()) {
            putString(CHAT_MESSAGE_CONTENT_PREFIX + chatMessage.id, chatMessage.content)
            putLong(CHAT_MESSAGE_TIMESTAMP_PREFIX + chatMessage.id,
                    chatMessage.timestamp.toDate().time)
            apply()
        }
    }

    private fun saveChatMessageToRemoteDB(chatMessage: ChatMessage) {
        db.collection(CHAT_MESSAGE_FIREBASE_COLLECTION)
                .document(chatMessage.id)
                .set(chatMessage)
                .addOnSuccessListener { df ->
                    Log.d("firebase", "DocumentSnapshot added with ID: ${chatMessage.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("firebase", "Error adding document", e)
                }
    }


    private fun restoreChatMessages() {
        db.collection(CHAT_MESSAGE_FIREBASE_COLLECTION)
                .get()
                .addOnSuccessListener { result ->
                    Log.d("firebase", "Restored ${result.size()} messages from Firebase.")
                    chatMessages = ArrayList(result.toObjects(ChatMessage::class.java))
                    chatMessages.forEach {
                        messageIDs.add(it.id)
                        saveChatMessageToLocalDB(it)
                    }
                    submitNewChatMessagesArray()
                }
                .addOnFailureListener { exception ->
                    Log.w("firebase", "Error getting documents.", exception)
                    Thread {
                        chatMessages = restoreChatMessagesLocalDB()
                        submitNewChatMessagesArray()
                    }.start()
                }
    }

    private fun submitNewChatMessagesArray() = runOnUiThread { adapter.submitList(chatMessages) }

    private fun restoreChatMessagesLocalDB(): ArrayList<ChatMessage> {
        messageIDs = sharedPref.getStringSet(CHAT_MESSAGES_IDS, mutableSetOf()) ?: HashSet()
        val chatMessagesTmp = ArrayList<ChatMessage>(messageIDs.size)
        for (id in messageIDs) {
            val content = sharedPref.getString(CHAT_MESSAGE_CONTENT_PREFIX + id, null)
            val timestampLong = sharedPref.getLong(CHAT_MESSAGE_TIMESTAMP_PREFIX + id, -1)

            if (content != null && timestampLong == -1L)
                continue

            val timestamp = Timestamp(Date(timestampLong))
            // This shouldn't warn me, but it does, so I used this idiom
            chatMessagesTmp.add(ChatMessage(id, content ?: "", timestamp))
        }
        chatMessagesTmp.sortBy { it.timestamp }
        Log.i("restoreChatMessages", "Restored ${chatMessagesTmp.size} messages.")
        return chatMessagesTmp
    }

    companion object {

        internal const val EDIT_TEXT_STRING_KEY = "editText"
        // Chat Messages constants
        internal const val CHAT_MESSAGES_IDS = "chat_message_ids"
        internal const val CHAT_MESSAGE_CONTENT_PREFIX = "message_content_"
        internal const val CHAT_MESSAGE_TIMESTAMP_PREFIX = "message_timestamp_"
        internal const val CHAT_MESSAGE_FIREBASE_COLLECTION = "chat_messages"
    }
}
