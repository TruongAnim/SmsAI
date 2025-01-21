package com.truonganim.sms.ai.domain.model

data class Call(
    val id: Long,
    val number: String,
    val name: String?,
    val timestamp: Long,
    val duration: Long,
    val type: CallType,
    val isNew: Boolean
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    UNKNOWN
} 