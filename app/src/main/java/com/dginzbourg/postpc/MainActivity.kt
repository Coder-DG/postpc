package com.dginzbourg.postpc

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore

import java.util.ArrayList


class MainActivity : AppCompatActivity() {


    private var adapter = MessageRecyclerUtils.ChatMessageAdapter(OnItemClickCallback())
    private val messageIDs = HashSet<String>()
    private var chatMessages = ArrayList<ChatMessage>()
    private lateinit var editText: EditText
    private val db = FirebaseFirestore.getInstance()

    private fun getAvailableMessageID(): String {
        var id: String
        do {
            id = Utils.generateMessageId()
        } while (id !in messageIDs)
        return id
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        }
        val chatMessagesCopy = ArrayList(chatMessages)
        val messageId = getAvailableMessageID()
        chatMessagesCopy.add(ChatMessage(messageId, text))
        messageIDs.add(messageId)
        Log.d("handleSend", "Inserted message: $text")
        chatMessages = chatMessagesCopy
        adapter.submitList(chatMessages)
        editText.setText("")
        saveRecyclerView()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let { restoreTextViews(savedInstanceState) }
    }

    private fun restoreTextViews(savedInstanceState: Bundle) {
        editTextString = savedInstanceState.getString(EDIT_TEXT_STRING_KEY)
    }

    private fun restoreRecyclerView() {
        with(this.getSharedPreferences(
                getString(R.string.shared_pref_chat_messages), Context.MODE_PRIVATE)) {
            val messageCount = getInt(CHAT_MESSAGES_COUNT, 0)
            chatMessages = ArrayList(messageCount)
            for (i in 0 until messageCount) {
                val message = getString(CHAT_MESSAGES_KEY_PREFIX + i, "")
                // This shouldn't warn me, but it does, so I used this idiom
                chatMessages.add(ChatMessage(message ?: ""))
            }
        }
        Log.i("restoreRecyclerView", "Restored ${chatMessages.size} messages.")
        adapter.submitList(chatMessages)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveTextViews(outState)
    }

    private fun saveTextViews(outState: Bundle) {
        outState.putString(EDIT_TEXT_STRING_KEY, editTextString)
    }

    private fun saveRecyclerView() {
        val messageCount = chatMessages.size
        val sharedPref = this.getSharedPreferences(
                getString(R.string.shared_pref_chat_messages), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt(CHAT_MESSAGES_COUNT, messageCount)
            for (i in 0 until messageCount) {
                putString(CHAT_MESSAGES_KEY_PREFIX + i, chatMessages[i].message)
            }

            apply()
        }
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
        // TODO(This will be removeMessage(messageID))
        val chatMessagesCopy = ArrayList(chatMessages)
        chatMessagesCopy.removeAt(pos)
        chatMessages = chatMessagesCopy
        adapter.submitList(chatMessages)
        saveRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        restoreRecyclerView()
    }

    override fun onPause() {
        super.onPause()
        saveRecyclerView()
    }

    companion object {

        internal val CHAT_MESSAGES_COUNT = "message_count_"
        internal val EDIT_TEXT_STRING_KEY = "editText"
        internal val CHAT_MESSAGES_KEY_PREFIX = "message_"
        internal var editTextString: String? = ""
    }
}
