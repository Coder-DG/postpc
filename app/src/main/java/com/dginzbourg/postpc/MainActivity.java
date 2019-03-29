package com.dginzbourg.postpc;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    static final String CHAT_MESSAGES_COUNT = "message_count_";
    static final String EDIT_TEXT_STRING_KEY = "editText";
    static final String CHAT_MESSAGES_KEY_PREFIX = "message_";

    MessageRecyclerUtils.ChatMessageAdapter adapter = new MessageRecyclerUtils.ChatMessageAdapter();
    ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    static String editTextString = "";
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.edit_text);
        editText.setText(editTextString);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    handleSend();
                } catch (SendException e) {
                    Utils.showToast(
                            MainActivity.this, e.getMessage(), R.integer.toast_duration);
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.chat_messages_recycler);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false)
        );
        recyclerView.setAdapter(adapter);
    }

    private void handleSend() throws SendException {
        String text = editText.getText().toString();
        if (text.isEmpty()) {
            throw new SendException(getString(R.string.empty_message_error));
        }
        ArrayList<ChatMessage> chatMessagesCopy = new ArrayList<>(chatMessages);
        chatMessagesCopy.add(new ChatMessage(text));
        Log.d("handleSend", "Inserted message: " + text);
        chatMessages = chatMessagesCopy;
        adapter.submitList(chatMessages);
        editText.setText("");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            restoreTextViews(savedInstanceState);
            restoreRecyclerView(savedInstanceState);
        }
    }

    private void restoreTextViews(@NonNull Bundle savedInstanceState) {
        editTextString = savedInstanceState.getString(EDIT_TEXT_STRING_KEY);
    }

    private void restoreRecyclerView(@NonNull Bundle savedInstanceState) {
        int messageCount = savedInstanceState.getInt(CHAT_MESSAGES_COUNT);
        chatMessages = new ArrayList<>(messageCount);
        for (int i = 0; i < messageCount; i++) {
            String message = savedInstanceState.getString(CHAT_MESSAGES_KEY_PREFIX + i);
            chatMessages.add(new ChatMessage(message));
        }
        Log.d("restoreRecyclerView", "Restored " + messageCount + " messages.");
        adapter.submitList(chatMessages);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        saveTextViews(outState);
        saveRecyclerView(outState);
    }

    private void saveTextViews(@NonNull Bundle outState) {
        outState.putString(EDIT_TEXT_STRING_KEY, editTextString);
    }

    private void saveRecyclerView(@NonNull Bundle outState) {
        int messageCount = chatMessages.size();
        outState.putInt(CHAT_MESSAGES_COUNT, messageCount);
        for (int i = 0; i < messageCount; i++) {
            outState.putString(CHAT_MESSAGES_KEY_PREFIX + i, chatMessages.get(i).getMessage());
        }
    }
}