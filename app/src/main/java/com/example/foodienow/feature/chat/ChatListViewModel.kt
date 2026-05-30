package com.example.foodienow.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.ChatSummary
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val isLoading: Boolean = true,
    val chats: List<ChatSummary> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadChats()
        observeChatChanges()
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = authRepository.getAuthState().firstOrNull()
                val currentUserId = user?.id ?: return@launch

                val summaries = chatRepository.getChatSummaries(currentUserId)
                _uiState.update { it.copy(chats = summaries, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun observeChatChanges() {
        viewModelScope.launch {
            val user = authRepository.getAuthState().firstOrNull()
            val currentUserId = user?.id ?: return@launch

            chatRepository.listenToAnyMessageChanges(currentUserId).collect {
                try {
                    val summaries = chatRepository.getChatSummaries(currentUserId)
                    _uiState.update { it.copy(chats = summaries) }
                } catch (e: Exception) {
                }
            }
        }
    }
}