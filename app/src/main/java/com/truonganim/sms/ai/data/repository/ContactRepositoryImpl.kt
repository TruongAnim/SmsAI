package com.truonganim.sms.ai.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.truonganim.sms.ai.domain.model.Contact
import com.truonganim.sms.ai.domain.model.PhoneNumber
import com.truonganim.sms.ai.domain.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ContactRepository {

    companion object {
        private const val TAG = "ContactRepository"
    }

    private val mutex = Mutex()
    private val contactsCache = mutableMapOf<String, Contact>()
    private val phoneNumberToContactIdMap = mutableMapOf<String, Long>()
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    override suspend fun loadContacts() {
        if (contactsCache.isNotEmpty()) return
        
        mutex.withLock {
            if (contactsCache.isNotEmpty()) return
            
            Log.d(TAG, "Loading all contacts...")
            _isLoading.value = true
            val startTime = System.currentTimeMillis()

            try {
                // First, get all contacts with their basic info
                val contactsById = mutableMapOf<Long, Contact>()
                context.contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    arrayOf(
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER
                    ),
                    "${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0",
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        try {
                            val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                            val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                            val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))
                            val lookupKey = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY))

                            contactsById[id] = Contact(
                                id = id,
                                name = name ?: "",
                                phoneNumbers = mutableListOf(),
                                photoUri = photoUri,
                                lookupKey = lookupKey
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing contact", e)
                        }
                    }
                }

                Log.d(TAG, "Loaded ${contactsById.size} contacts with basic info")

                // Then get all phone numbers
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                    ),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        try {
                            val contactId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                            val number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val normalizedNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))

                            if (!number.isNullOrBlank()) {
                                val contact = contactsById[contactId]
                                if (contact != null) {
                                    (contact.phoneNumbers as MutableList).add(
                                        PhoneNumber(number = number, normalizedNumber = normalizedNumber)
                                    )
                                    // Map both normal and normalized numbers to contact ID
                                    phoneNumberToContactIdMap[number] = contactId
                                    if (!normalizedNumber.isNullOrBlank()) {
                                        phoneNumberToContactIdMap[normalizedNumber] = contactId
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing phone number", e)
                        }
                    }
                }

                // Store contacts in cache by their phone numbers
                contactsById.values.forEach { contact ->
                    contact.phoneNumbers.forEach { phoneNumber ->
                        contactsCache[phoneNumber.number] = contact
                        if (phoneNumber.normalizedNumber != null) {
                            contactsCache[phoneNumber.normalizedNumber] = contact
                        } else {
                            // Manually normalize and cache if system normalized number is null
                            val manuallyNormalizedNumber = normalizePhoneNumber(phoneNumber.number)
                            contactsCache[manuallyNormalizedNumber] = contact
                        }
                    }
                }

                val endTime = System.currentTimeMillis()
                Log.d(TAG, "Loaded and cached ${contactsCache.size} phone numbers for ${contactsById.size} contacts in ${endTime - startTime}ms")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading contacts", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    override suspend fun getContactByPhoneNumber(phoneNumber: String): Contact? {
        loadContacts() // Ensure contacts are loaded
        return contactsCache[phoneNumber] ?: contactsCache[normalizePhoneNumber(phoneNumber)]
    }

    override suspend fun searchContacts(query: String): List<Contact> {
        loadContacts() // Ensure contacts are loaded
        return contactsCache.values
            .distinctBy { it.id }
            .filter { contact ->
                contact.name.contains(query, ignoreCase = true) ||
                contact.phoneNumbers.any { it.number.contains(query) }
            }
            .sortedBy { it.name }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^0-9+]"), "")
    }
} 