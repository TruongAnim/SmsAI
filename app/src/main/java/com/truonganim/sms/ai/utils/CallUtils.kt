package com.truonganim.sms.ai.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object CallUtils {
    private const val TAG = "CallUtils"

    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${PhoneNumberUtils.normalizePhoneNumber(phoneNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error making phone call", e)
            // Fallback to dial action if call permission is not granted
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${PhoneNumberUtils.normalizePhoneNumber(phoneNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }
} 