package com.truonganim.sms.ai.ui.screens.calls

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onCallClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is CallUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.calls) { call ->
                        CallItem(
                            call = call,
                            onCallClick = { onCallClick(call.number) },
                            onDeleteClick = { viewModel.deleteCallLog(call.id) }
                        )
                    }
                }
            }
            is CallUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is CallUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallItem(
    call: Call,
    onCallClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Call Log") },
            text = { Text("Are you sure you want to delete this call log?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    ListItem(
        headlineContent = {
            Text(
                text = call.name ?: call.number,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                if (call.name != null) {
                    Text(
                        text = call.number,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatDate(call.timestamp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (call.duration > 0) {
                        Text(
                            text = formatDuration(call.duration),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = "Call type",
                modifier = when (call.type) {
                    CallType.INCOMING -> Modifier.rotate(180f)
                    CallType.OUTGOING -> Modifier
                    CallType.MISSED -> Modifier.rotate(135f)
                    CallType.REJECTED -> Modifier.rotate(135f)
                    CallType.BLOCKED -> Modifier.rotate(135f)
                    CallType.UNKNOWN -> Modifier
                },
                tint = when (call.type) {
                    CallType.MISSED, CallType.REJECTED, CallType.BLOCKED -> MaterialTheme.colorScheme.error
                    CallType.INCOMING -> MaterialTheme.colorScheme.primary
                    CallType.OUTGOING -> MaterialTheme.colorScheme.secondary
                    else -> LocalContentColor.current
                }
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onCallClick) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call"
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
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