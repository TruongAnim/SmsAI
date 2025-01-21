package com.truonganim.sms.ai.ui.screens.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.sms.ai.domain.model.Call
import com.truonganim.sms.ai.domain.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CallUiState {
    object Loading : CallUiState()
    data class Success(val calls: List<Call>) : CallUiState()
    data class Error(val message: String) : CallUiState()
}

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CallUiState>(CallUiState.Loading)
    val uiState: StateFlow<CallUiState> = _uiState

    init {
        loadCallLogs()
    }

    fun refresh() {
        loadCallLogs()
    }

    fun deleteCallLog(id: Long) {
        viewModelScope.launch {
            try {
                callRepository.deleteCallLog(id)
                loadCallLogs() // Refresh the list after deletion
            } catch (e: Exception) {
                _uiState.value = CallUiState.Error(e.message ?: "Error deleting call log")
            }
        }
    }

    private fun loadCallLogs() {
        viewModelScope.launch {
            _uiState.value = CallUiState.Loading
            try {
                val calls = callRepository.getCallLogs()
                _uiState.value = CallUiState.Success(calls)
            } catch (e: Exception) {
                _uiState.value = CallUiState.Error(e.message ?: "Error loading call logs")
            }
        }
    }
} 