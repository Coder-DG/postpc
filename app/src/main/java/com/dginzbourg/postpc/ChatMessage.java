package com.dginzbourg.postpc;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage  {
    private final String message;
    private static long nextMessageId = 0;
    private long messageId;

    public ChatMessage(String message) {
        this.message = message;
        messageId = nextMessageId;
        nextMessageId += 1;
    }



    public String getMessage() {
        return message;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

}
