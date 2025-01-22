package com.truonganim.sms.ai.utils

object PhoneNumberUtils {
    /**
     * Normalizes a phone number by removing all characters except digits and '+'.
     * Does not modify the number format, just cleans it.
     */
    fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^0-9+]"), "")
    }

    /**
     * Formats a phone number to international format with country code.
     * Use this when you need to ensure the number has a country code.
     */
    fun formatToInternational(phoneNumber: String, defaultCountryCode: String = "84"): String {
        var normalized = normalizePhoneNumber(phoneNumber)
        
        // If it's already in international format, return as is
        if (normalized.startsWith("+")) {
            return normalized
        }
        
        // Remove leading zeros
        normalized = normalized.trimStart('0')
        
        // For numbers without country code
        if (normalized.length <= 10) {
            normalized = "+$defaultCountryCode$normalized"
        } else if (!normalized.startsWith("+")) {
            normalized = "+$normalized"
        }
        
        return normalized
    }
} 