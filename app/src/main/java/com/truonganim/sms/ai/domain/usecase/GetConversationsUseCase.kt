package com.truonganim.sms.ai.domain.usecase

import com.truonganim.sms.ai.domain.model.Conversation
import com.truonganim.sms.ai.domain.repository.MessageRepository
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(): List<Conversation> {
        return messageRepository.getConversations()
    }
} 