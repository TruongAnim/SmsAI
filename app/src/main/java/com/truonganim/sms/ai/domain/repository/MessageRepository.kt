package com.truonganim.sms.ai.domain.repository

import com.truonganim.sms.ai.domain.model.Conversation
import com.truonganim.sms.ai.domain.model.Message

interface MessageRepository {
    suspend fun getConversations(): List<Conversation>
    suspend fun getMessages(threadId: Long): List<Message>
    suspend fun sendMessage(address: String, body: String): Result<Unit>
    suspend fun getThreadIdForAddress(address: String): Long
} 