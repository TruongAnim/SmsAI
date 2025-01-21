package com.truonganim.sms.ai.domain.model

data class Message(
    val id: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val type: MessageType,
    val read: Boolean = true,
    val threadId: Long
)

enum class MessageType {
    INBOX,
    SENT,
    DRAFT,
    OUTBOX,
    FAILED,
    QUEUED
} 