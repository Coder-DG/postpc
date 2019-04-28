package com.dginzbourg.postpc

class ChatMessage(val message: String) {
    var messageId: Long = 0

    init {
        messageId = nextMessageId
        nextMessageId += 1
    }

    companion object {
        private var nextMessageId: Long = 0
    }

}
