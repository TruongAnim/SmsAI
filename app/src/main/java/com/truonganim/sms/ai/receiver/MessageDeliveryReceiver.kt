package com.truonganim.sms.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Activity

class MessageDeliveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                // Message delivered successfully
            }
            Activity.RESULT_CANCELED -> {
                // Message delivery failed
            }
        }
    }
} 