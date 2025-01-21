package com.truonganim.sms.ai.domain.model

data class Conversation(
    val threadId: Long,
    val address: String,
    val messages: List<Message>,
) {
    val lastMessage: Message
        get() = messages.maxBy { it.timestamp }

    val snippet: String
        get() = lastMessage.body

    val timestamp: Long
        get() = lastMessage.timestamp

    val messageCount: Int
        get() = messages.size

    val unreadCount: Int
        get() = messages.count { !it.read }

    val lastMessageType: MessageType
        get() = lastMessage.type

    companion object {
        fun groupMessagesByThread(messages: List<Message>): List<Conversation> {
            return messages
                .groupBy { it.threadId }
                .map { (threadId, threadMessages) ->
                    Conversation(
                        threadId = threadId,
                        address = threadMessages.first().address,
                        messages = threadMessages.sortedByDescending { it.timestamp }
                    )
                }
                .sortedByDescending { it.timestamp }
        }
    }
} 