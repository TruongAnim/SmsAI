package com.truonganim.sms.ai.ui.screens.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.sms.ai.domain.model.Contact
import com.truonganim.sms.ai.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NewConversationUiState {
    object Loading : NewConversationUiState()
    data class Success(val contacts: List<Contact>) : NewConversationUiState()
    data class Error(val message: String) : NewConversationUiState()
}

@HiltViewModel
class NewConversationViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewConversationUiState>(NewConversationUiState.Loading)
    val uiState: StateFlow<NewConversationUiState> = _uiState

    init {
        loadContacts()
    }

    fun searchContacts(query: String) {
        viewModelScope.launch {
            try {
                val contacts = if (query.isBlank()) {
                    contactRepository.getContacts()
                } else {
                    contactRepository.searchContacts(query)
                }
                _uiState.value = NewConversationUiState.Success(contacts)
            } catch (e: Exception) {
                _uiState.value = NewConversationUiState.Error(e.message ?: "Error searching contacts")
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            try {
                val contacts = contactRepository.getContacts()
                _uiState.value = NewConversationUiState.Success(contacts)
            } catch (e: Exception) {
                _uiState.value = NewConversationUiState.Error(e.message ?: "Error loading contacts")
            }
        }
    }
} 