package com.truonganim.sms.ai.ui.screens.calls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.truonganim.sms.ai.domain.model.Call
import com.truonganim.sms.ai.domain.model.CallType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallScreen(
    viewModel: CallViewModel,
    onCallClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    onCallItemClick: (Call) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is CallUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        is CallUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = (uiState as CallUiState.Error).message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is CallUiState.Success -> {
            val calls = (uiState as CallUiState.Success).calls
            LazyColumn {
                items(calls) { call ->
                    CallItem(
                        call = call,
                        onCallClick = onCallClick,
                        onMessageClick = onMessageClick,
                        onItemClick = onCallItemClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallItem(
    call: Call,
    onCallClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    onItemClick: (Call) -> Unit
) {
    ListItem(
        modifier = Modifier
            .clickable { onItemClick(call) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        headlineContent = {
            Text(
                text = call.name ?: call.number,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = if (call.name != null) call.number else "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = { onCallClick(call.number) }) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call"
                    )
                }
                IconButton(onClick = { onMessageClick(call.number) }) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Message"
                    )
                }
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val date = Date(timestamp)
    return when {
        timestamp > now - 24 * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault())
        timestamp > now - 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE", Locale.getDefault())
        else -> SimpleDateFormat("MMM d", Locale.getDefault())
    }.format(date)
}

private fun formatDuration(duration: Long): String {
    val hours = duration / 3600
    val minutes = (duration % 3600) / 60
    val seconds = duration % 60
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%d:%02d", minutes, seconds)
    }
} 