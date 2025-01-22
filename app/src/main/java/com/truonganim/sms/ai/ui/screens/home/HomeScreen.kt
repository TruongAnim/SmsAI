package com.truonganim.sms.ai.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.truonganim.sms.ai.ui.screens.calls.CallScreen
import com.truonganim.sms.ai.ui.screens.calls.CallViewModel
import com.truonganim.sms.ai.utils.CallUtils

enum class HomeTab {
    CALLS, MESSAGES, NOTES, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onConversationClick: (Long, String) -> Unit,
    onNewConversation: () -> Unit,
    onCallMessage: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(HomeTab.MESSAGES) }
    val callViewModel: CallViewModel = hiltViewModel()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "SMS AI",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.CALLS,
                    onClick = { selectedTab = HomeTab.CALLS },
                    icon = { Icon(Icons.Filled.Phone, contentDescription = "Calls") },
                    label = { Text("Calls") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.MESSAGES,
                    onClick = { selectedTab = HomeTab.MESSAGES },
                    icon = { Icon(Icons.Filled.Email, contentDescription = "Messages") },
                    label = { Text("Messages") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.NOTES,
                    onClick = { selectedTab = HomeTab.NOTES },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = "Notes") },
                    label = { Text("Notes") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.SETTINGS,
                    onClick = { selectedTab = HomeTab.SETTINGS },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == HomeTab.MESSAGES) {
                FloatingActionButton(
                    onClick = onNewConversation,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Conversation")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                HomeTab.CALLS -> {
                    CallScreen(
                        viewModel = callViewModel,
                        onCallClick = { number -> 
                            CallUtils.makePhoneCall(context, number)
                        },
                        onMessageClick = onCallMessage
                    )
                }
                HomeTab.MESSAGES -> {
                    MessagesTab(
                        viewModel = viewModel,
                        onConversationClick = onConversationClick
                    )
                }
                HomeTab.NOTES -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Notes Coming Soon",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                HomeTab.SETTINGS -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Settings Coming Soon",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
} 