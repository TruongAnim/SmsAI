package com.truonganim.sms.ai.ui.screens.splash

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToPermission: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        delay(1000) // Show splash for 1 second

        // Check if we're the default SMS app
        val isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        
        // Check if we have all required permissions
        val hasReadSms = context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val hasSendSms = context.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val hasReceiveSms = context.checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val hasReceiveMms = context.checkSelfPermission(Manifest.permission.RECEIVE_MMS) == PackageManager.PERMISSION_GRANTED
        val hasReceiveWapPush = context.checkSelfPermission(Manifest.permission.RECEIVE_WAP_PUSH) == PackageManager.PERMISSION_GRANTED

        val hasAllPermissions = hasReadSms && hasSendSms && hasReceiveSms && hasReceiveMms && hasReceiveWapPush && isDefaultSmsApp

        if (hasAllPermissions) {
            onNavigateToMain()
        } else {
            onNavigateToPermission()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SMS AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Smart Message Assistant",
                fontSize = 16.sp
            )
        }
    }
} 