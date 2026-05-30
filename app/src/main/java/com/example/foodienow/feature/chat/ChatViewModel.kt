package com.example.foodienow.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Message
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = true,
    val currentUserId: String = "",
    val messages: List<Message> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val storeId: String = checkNotNull(savedStateHandle["storeId"])
    private val receiverId: String = checkNotNull(savedStateHandle["receiverId"])

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val user = authRepository.resolveStoredSession()
                val currentUserId = user?.id ?: return@launch

                _uiState.update { it.copy(currentUserId = currentUserId) }

                chatRepository.markMessagesAsRead(
                    storeId = storeId,
                    partnerId = receiverId,
                    currentUserId = currentUserId
                )

                // Tải lịch sử chat
                val history = chatRepository.getChatHistory(storeId, currentUserId, receiverId)
                _uiState.update { it.copy(messages = history, isLoading = false) }

                chatRepository.listenToMessages(storeId).collect { newMessage ->
                    val isRelevant = (newMessage.senderId == currentUserId && newMessage.receiverId == receiverId) ||
                            (newMessage.senderId == receiverId && newMessage.receiverId == currentUserId)

                    if (isRelevant) {
                        _uiState.update { state ->
                            if (state.messages.any { it.id == newMessage.id }) {
                                state
                            } else {
                                state.copy(messages = state.messages + newMessage)
                            }
                        }
                        if (newMessage.senderId == receiverId && newMessage.receiverId == currentUserId && !newMessage.isRead) {
                            chatRepository.markMessagesAsRead(
                                storeId = storeId,
                                partnerId = receiverId,
                                currentUserId = currentUserId
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        val currentState = _uiState.value
        val senderId = currentState.currentUserId
        if (senderId.isEmpty()) return

        val newMessage = Message(
            senderId = senderId,
            receiverId = receiverId,
            storeId = storeId,
            content = content.trim()
        )

        viewModelScope.launch {
            chatRepository.sendMessage(newMessage)
        }
    }
}