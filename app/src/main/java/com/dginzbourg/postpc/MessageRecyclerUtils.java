package com.dginzbourg.postpc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

class MessageRecyclerUtils {
    static class ChatMessageHolder extends RecyclerView.ViewHolder {
        final TextView chatMessageTextView;

        ChatMessageHolder(@NonNull View itemView) {
            super(itemView);
            chatMessageTextView = itemView.findViewById(R.id.message_text_view);
        }

        void setMessage(String message) {
            chatMessageTextView.setText(message);
        }
    }

    static class ChatMessageCallBack extends DiffUtil.ItemCallback<ChatMessage> {
        @Override
        public boolean areItemsTheSame(@NonNull ChatMessage m1, @NonNull ChatMessage m2) {
            return m1.getMessageId() == m2.getMessageId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatMessage m1, @NonNull ChatMessage m2) {
            return m1.getMessage().equals(m2.getMessage());
        }
    }

    static class ChatMessageAdapter extends ListAdapter<ChatMessage, ChatMessageHolder> {

        ChatMessageAdapter() {
            super(new ChatMessageCallBack());
        }

        @NonNull
        @Override
        public ChatMessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            Context context = parent.getContext();
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_one_message, parent, false);
            return new ChatMessageHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatMessageHolder chatMessageHolder, int pos) {
            ChatMessage chatMessage = getItem(pos);
            chatMessageHolder.setMessage(chatMessage.getMessage());
        }
    }
}
