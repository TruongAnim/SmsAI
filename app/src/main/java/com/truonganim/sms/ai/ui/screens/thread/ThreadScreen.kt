package com.truonganim.sms.ai.ui.screens.thread

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.truonganim.sms.ai.domain.model.Message
import com.truonganim.sms.ai.domain.model.MessageType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    viewModel: ThreadViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }

    // Handle system back button press
    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is ThreadUiState.Success -> {
                            Column {
                                Text(
                                    text = state.contactName ?: state.address,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (state.contactName != null) {
                                    Text(
                                        text = state.address,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        else -> Text("")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.padding(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Type a message") },
                    maxLines = 4,
                    enabled = (uiState as? ThreadUiState.Success)?.isSending != true
                )
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank() && (uiState as? ThreadUiState.Success)?.isSending != true
                ) {
                    if ((uiState as? ThreadUiState.Success)?.isSending == true) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Send message")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ThreadUiState.Success -> {
                    MessageList(
                        messages = state.messages,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Show error message if any
                    state.errorMessage?.let { error ->
                        Snackbar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            action = {
                                TextButton(onClick = { viewModel.clearError() }) {
                                    Text("DISMISS")
                                }
                            }
                        ) {
                            Text(error)
                        }
                    }
                }
                is ThreadUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ThreadUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        reverseLayout = true
    ) {
        items(messages) { message ->
            MessageItem(message = message)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MessageItem(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isIncoming = message.type == MessageType.INBOX
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isIncoming) Arrangement.Start else Arrangement.End
    ) {
        Card(
            modifier = Modifier.widthIn(max = 340.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isIncoming) 
                    MaterialTheme.colorScheme.secondaryContainer 
                else 
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.body,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(if (isIncoming) Alignment.Start else Alignment.End)
                )
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
} 