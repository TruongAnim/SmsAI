package com.truonganim.sms.ai.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings

enum class HomeTab {
    CALLS, MESSAGES, NOTES, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onConversationClick: (threadId: Long, address: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(HomeTab.MESSAGES) }

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
                    icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                    label = { Text("Calls") },
                    selected = selectedTab == HomeTab.CALLS,
                    onClick = { selectedTab = HomeTab.CALLS }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Email, contentDescription = "Messages") },
                    label = { Text("Messages") },
                    selected = selectedTab == HomeTab.MESSAGES,
                    onClick = { selectedTab = HomeTab.MESSAGES }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Notes") },
                    label = { Text("Notes") },
                    selected = selectedTab == HomeTab.NOTES,
                    onClick = { selectedTab = HomeTab.NOTES }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == HomeTab.SETTINGS,
                    onClick = { selectedTab = HomeTab.SETTINGS }
                )
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
                    // TODO: Implement Calls screen
                    Text("Calls Screen")
                }
                HomeTab.MESSAGES -> {
                    MessagesTab(
                        viewModel = viewModel,
                        onConversationClick = onConversationClick
                    )
                }
                HomeTab.NOTES -> {
                    // TODO: Implement Notes screen
                    Text("Notes Screen")
                }
                HomeTab.SETTINGS -> {
                    // TODO: Implement Settings screen
                    Text("Settings Screen")
                }
            }
        }
    }
} 