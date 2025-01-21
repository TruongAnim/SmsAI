package com.truonganim.sms.ai.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MessagesTab(
    viewModel: HomeViewModel,
    onConversationClick: (threadId: Long, address: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HomeUiState.Success -> {
                ConversationList(
                    conversations = (uiState as HomeUiState.Success).conversations,
                    onConversationClick = { threadId ->
                        val conversation = (uiState as HomeUiState.Success)
                            .conversations
                            .find { it.threadId == threadId }
                        if (conversation != null) {
                            onConversationClick(threadId, conversation.address)
                        }
                    }
                )
            }
            is HomeUiState.Error -> {
                Text(
                    text = (uiState as HomeUiState.Error).message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
} 