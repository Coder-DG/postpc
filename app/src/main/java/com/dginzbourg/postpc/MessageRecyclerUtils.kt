package com.dginzbourg.postpc

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

internal class MessageRecyclerUtils {
    internal interface ChatMessageLongClickCallBack {
        fun onLongClick(pos: Int): Boolean
    }

    internal class ChatMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatMessageTextView: TextView

        init {
            chatMessageTextView = itemView.findViewById(R.id.message_text_view)
        }

        fun setMessage(message: String) {
            chatMessageTextView.text = message
        }
    }

    internal class ChatMessageCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(m1: ChatMessage, m2: ChatMessage): Boolean {
            return m1.id == m2.id
        }

        override fun areContentsTheSame(m1: ChatMessage, m2: ChatMessage): Boolean {
            return m1.content == m2.content && m1.timestamp == m2.timestamp
        }
    }

    internal class ChatMessageAdapter(val chatMessagesCallback: ChatMessageLongClickCallBack) :
            ListAdapter<ChatMessage, ChatMessageHolder>(ChatMessageCallback()) {


        override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ChatMessageHolder {
            val context = parent.context
            val itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_one_message, parent, false)
            val holder = ChatMessageHolder(itemView)
            itemView.setOnLongClickListener {
                chatMessagesCallback.onLongClick(holder.getAdapterPosition())
            }
            return holder
        }

        override fun onBindViewHolder(chatMessageHolder: ChatMessageHolder, pos: Int) {
            val chatMessage = getItem(pos)
            chatMessageHolder.setMessage(chatMessage.content)
        }
    }
}
