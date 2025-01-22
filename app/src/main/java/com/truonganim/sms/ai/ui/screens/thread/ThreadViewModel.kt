package com.truonganim.sms.ai.ui.screens.thread

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.truonganim.sms.ai.domain.model.Message
import com.truonganim.sms.ai.domain.repository.MessageRepository
import com.truonganim.sms.ai.domain.repository.ContactRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ThreadUiState {
    data class Success(
        val address: String,
        val contactName: String?,
        val messages: List<Message>,
        val isSending: Boolean = false,
        val errorMessage: String? = null
    ) : ThreadUiState()
    object Loading : ThreadUiState()
    data class Error(val message: String) : ThreadUiState()
}

class ThreadViewModel @AssistedInject constructor(
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    @Assisted private val threadId: Long,
    @Assisted private val address: String
) : ViewModel() {
    companion object {
        private const val TAG = "ThreadViewModel"
    }

    private val _uiState = MutableStateFlow<ThreadUiState>(ThreadUiState.Loading)
    val uiState: StateFlow<ThreadUiState> = _uiState

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading messages for threadId: $threadId, address: $address")
                val messages = messageRepository.getMessages(threadId)
                val contact = contactRepository.getContactByPhoneNumber(address)
                _uiState.value = ThreadUiState.Success(
                    address = address,
                    contactName = contact?.name,
                    messages = messages
                )
            } catch (e: Exception) {
                _uiState.value = ThreadUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ThreadUiState.Success) return@launch

            // Update state to show sending
            _uiState.value = currentState.copy(isSending = true, errorMessage = null)

            try {
                messageRepository.sendMessage(address, text).onSuccess {
                    // Reload messages after successful send
                    loadMessages()
                }.onFailure { error ->
                    // Show error but keep existing messages
                    _uiState.value = currentState.copy(
                        isSending = false,
                        errorMessage = error.message ?: "Failed to send message"
                    )
                }
            } catch (e: Exception) {
                // Show error but keep existing messages
                _uiState.value = currentState.copy(
                    isSending = false,
                    errorMessage = e.message ?: "Failed to send message"
                )
            }
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is ThreadUiState.Success) {
            _uiState.value = currentState.copy(errorMessage = null)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(threadId: Long, address: String): ThreadViewModel
    }
}

@HiltViewModel
class ThreadViewModelFactoryProvider @Inject constructor(
    private val factory: ThreadViewModel.Factory
) : ViewModel() {
    fun createViewModelFactory(threadId: Long, address: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(threadId, address) as T
            }
        }
} 