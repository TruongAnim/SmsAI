package com.truonganim.sms.ai.domain.model

data class Conversation(
    val threadId: Long,
    val address: String,
    val messages: List<Message>,
    val contact: Contact? = null
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

    // Display name prioritizes contact name over address
    val displayName: String
        get() = contact?.name ?: address

    // Get the best phone number match for this conversation
    val matchedPhoneNumber: PhoneNumber?
        get() = contact?.phoneNumbers?.find { 
            it.number == address || it.normalizedNumber == address 
        }

    companion object {
        fun groupMessagesByThread(messages: List<Message>, contacts: Map<String, Contact>): List<Conversation> {
            return messages
                .groupBy { it.threadId }
                .map { (threadId, threadMessages) ->
                    val address = threadMessages.first().address
                    val contact = contacts[address] ?: contacts.entries.find { (_, contact) ->
                        contact.phoneNumbers.any { phone -> 
                            phone.number == address || phone.normalizedNumber == address
                        }
                    }?.value
                    
                    Conversation(
                        threadId = threadId,
                        address = address,
                        messages = threadMessages,
                        contact = contact
                    )
                }
                .sortedByDescending { it.timestamp }
        }
    }
} 