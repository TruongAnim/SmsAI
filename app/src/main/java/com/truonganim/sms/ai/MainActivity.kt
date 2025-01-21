package com.truonganim.sms.ai

import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.truonganim.sms.ai.ui.navigation.NavGraph
import com.truonganim.sms.ai.ui.theme.SmsAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isDefaultSmsApp by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SmsAITheme {
                NavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check default SMS app status whenever the activity resumes
        isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this) == packageName
    }
}