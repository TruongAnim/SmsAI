package com.truonganim.sms.ai.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.sms.ai.domain.model.Conversation
import com.truonganim.sms.ai.domain.usecase.GetConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            try {
                val conversations = getConversationsUseCase()
                _uiState.value = MainUiState.Success(conversations)
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val conversations: List<Conversation>) : MainUiState()
    data class Error(val message: String) : MainUiState()
} 