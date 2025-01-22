package com.truonganim.sms.ai.domain.repository

import com.truonganim.sms.ai.domain.model.Contact
import kotlinx.coroutines.flow.StateFlow

interface ContactRepository {
    val isLoading: StateFlow<Boolean>
    
    suspend fun loadContacts()
    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact?
    suspend fun searchContacts(query: String): List<Contact>
    suspend fun getContacts(): List<Contact>
} 