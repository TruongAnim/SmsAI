package com.truonganim.sms.ai.data.repository

import android.content.Context
import android.provider.CallLog
import android.util.Log
import com.truonganim.sms.ai.domain.model.Call
import com.truonganim.sms.ai.domain.model.CallType
import com.truonganim.sms.ai.domain.repository.CallRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CallRepository {

    override suspend fun getCallLogs(): List<Call> {
        val calls = mutableListOf<Call>()
        try {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(CallLog.Calls._ID)
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
                val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
                val newIndex = cursor.getColumnIndex(CallLog.Calls.NEW)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val number = cursor.getString(numberIndex) ?: ""
                    val name = cursor.getString(nameIndex)
                    val date = cursor.getLong(dateIndex)
                    val duration = cursor.getLong(durationIndex)
                    val type = when (cursor.getInt(typeIndex)) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        else -> CallType.UNKNOWN
                    }
                    val isNew = cursor.getInt(newIndex) > 0

                    calls.add(
                        Call(
                            id = id,
                            number = number,
                            name = name,
                            timestamp = date,
                            duration = duration,
                            type = type,
                            isNew = isNew
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("CallRepository", "Error getting call logs", e)
            throw e
        }
        return calls
    }

    override suspend fun getCallLogsByNumber(number: String): List<Call> {
        return getCallLogs().filter { it.number == number }
    }

    override suspend fun deleteCallLog(id: Long) {
        try {
            context.contentResolver.delete(
                CallLog.Calls.CONTENT_URI,
                "${CallLog.Calls._ID} = ?",
                arrayOf(id.toString())
            )
        } catch (e: Exception) {
            Log.e("CallRepository", "Error deleting call log", e)
            throw e
        }
    }

    override suspend fun deleteAllCallLogs() {
        try {
            context.contentResolver.delete(
                CallLog.Calls.CONTENT_URI,
                null,
                null
            )
        } catch (e: Exception) {
            Log.e("CallRepository", "Error deleting all call logs", e)
            throw e
        }
    }
} 