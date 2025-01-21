package com.truonganim.sms.ai.domain.repository

import com.truonganim.sms.ai.domain.model.Call

interface CallRepository {
    suspend fun getCallLogs(): List<Call>
    suspend fun getCallLogsByNumber(number: String): List<Call>
    suspend fun deleteCallLog(id: Long)
    suspend fun deleteAllCallLogs()
} 