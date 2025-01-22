package com.truonganim.sms.ai.domain.model

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumbers: List<PhoneNumber>,
    val photoUri: String? = null,
    val lookupKey: String? = null
)

data class PhoneNumber(
    val number: String,
    val normalizedNumber: String
) 