package com.truonganim.sms.ai.ui.screens.permission

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissions = listOf(
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.SEND_SMS,
        android.Manifest.permission.RECEIVE_SMS,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.READ_CALL_LOG
    )

    // Launcher for default SMS app request
    val defaultSmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Telephony.Sms.getDefaultSmsPackage(context) == context.packageName) {
            onPermissionsGranted()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Launch default SMS app request
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(RoleManager::class.java)
                roleManager?.createRequestRoleIntent(RoleManager.ROLE_SMS)
            } else {
                Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                    putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                }
            }
            
            intent?.let {
                defaultSmsLauncher.launch(it)
            }
        } else {
            showRationale = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "SMS Permissions Required",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This app needs SMS permissions to function as your default messaging app.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.RECEIVE_MMS,
                            Manifest.permission.RECEIVE_WAP_PUSH,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_CALL_LOG
                        )
                    )
                }
            ) {
                Text("Grant Permissions")
            }
            
            if (showRationale) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Please grant all permissions to use the app",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 