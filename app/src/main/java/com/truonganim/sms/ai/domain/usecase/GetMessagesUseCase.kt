package com.truonganim.sms.ai.domain.usecase

import com.truonganim.sms.ai.domain.model.Message
import com.truonganim.sms.ai.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(threadId: Long): List<Message> {
        return messageRepository.getMessages(threadId)
    }
} 