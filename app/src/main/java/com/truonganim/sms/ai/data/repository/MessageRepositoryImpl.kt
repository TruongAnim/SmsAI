package com.truonganim.sms.ai.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.truonganim.sms.ai.domain.model.Conversation
import com.truonganim.sms.ai.domain.model.Message
import com.truonganim.sms.ai.domain.model.MessageType
import com.truonganim.sms.ai.domain.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MessageRepository {

    override suspend fun getConversations(): List<Conversation> {
        val messages = getAllMessages()
        return Conversation.groupMessagesByThread(messages)
    }

    private fun getAllMessages(threadId: Long? = null): List<Message> {
        val messages = mutableListOf<Message>()
        val selection = threadId?.let { "${Telephony.Sms.THREAD_ID} = ?" }
        val selectionArgs = threadId?.let { arrayOf(it.toString()) }
        
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ,
                Telephony.Sms.THREAD_ID
            ),
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIndex = it.getColumnIndexOrThrow(Telephony.Sms.READ)
            val threadIdIndex = it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)

            while (it.moveToNext()) {
                try {
                    messages.add(
                        Message(
                            id = it.getLong(idIndex),
                            address = it.getString(addressIndex) ?: "",
                            body = it.getString(bodyIndex) ?: "",
                            timestamp = it.getLong(dateIndex),
                            type = when (it.getInt(typeIndex)) {
                                Telephony.Sms.MESSAGE_TYPE_INBOX -> MessageType.INBOX
                                Telephony.Sms.MESSAGE_TYPE_SENT -> MessageType.SENT
                                Telephony.Sms.MESSAGE_TYPE_DRAFT -> MessageType.DRAFT
                                Telephony.Sms.MESSAGE_TYPE_OUTBOX -> MessageType.OUTBOX
                                Telephony.Sms.MESSAGE_TYPE_FAILED -> MessageType.FAILED
                                Telephony.Sms.MESSAGE_TYPE_QUEUED -> MessageType.QUEUED
                                else -> MessageType.INBOX
                            },
                            read = it.getInt(readIndex) == 1,
                            threadId = it.getLong(threadIdIndex)
                        )
                    )
                } catch (e: Exception) {
                    Log.e("MessageRepository", "Error processing message row", e)
                }
            }
        }

        return messages
    }

    private fun getAddressFromThreadId(contentResolver: ContentResolver, threadId: Long): String {
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI, // Changed from CONTENT_CONVERSATIONS_URI to SMS URI
            arrayOf(Telephony.Sms.ADDRESS),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} DESC LIMIT 1"
        )

        return cursor?.use {
            if (it.moveToFirst()) {
                try {
                    val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                    it.getString(addressIndex) ?: "Unknown"
                } catch (e: Exception) {
                    Log.e("MessageRepository", "Error getting address", e)
                    "Unknown"
                }
            } else "Unknown"
        } ?: "Unknown"
    }

    private fun getLastMessageType(contentResolver: ContentResolver, threadId: Long): MessageType {
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.TYPE),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} DESC LIMIT 1"
        )

        return cursor?.use {
            if (it.moveToFirst()) {
                try {
                    val typeIndex = it.getColumnIndexOrThrow(Telephony.Sms.TYPE)
                    when (it.getInt(typeIndex)) {
                        Telephony.Sms.MESSAGE_TYPE_INBOX -> MessageType.INBOX
                        Telephony.Sms.MESSAGE_TYPE_SENT -> MessageType.SENT
                        Telephony.Sms.MESSAGE_TYPE_DRAFT -> MessageType.DRAFT
                        Telephony.Sms.MESSAGE_TYPE_OUTBOX -> MessageType.OUTBOX
                        Telephony.Sms.MESSAGE_TYPE_FAILED -> MessageType.FAILED
                        Telephony.Sms.MESSAGE_TYPE_QUEUED -> MessageType.QUEUED
                        else -> MessageType.INBOX
                    }
                } catch (e: Exception) {
                    Log.e("MessageRepository", "Error getting message type", e)
                    MessageType.INBOX
                }
            } else MessageType.INBOX
        } ?: MessageType.INBOX
    }

    override suspend fun getMessages(threadId: Long): List<Message> {
        return getAllMessages(threadId)
    }

    override suspend fun sendMessage(address: String, body: String): Result<Unit> {
        return try {
            // TODO: Implement actual SMS sending
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 