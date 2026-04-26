package com.example.foodienow.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.AppNotification
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
    val errorMessage: String? = null
) {
    val unreadCount: Int = notifications.count { !it.isRead }
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = authRepository.getAuthState().first()
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notifications = emptyList(),
                        errorMessage = "Khong tim thay phien dang nhap."
                    )
                }
                return@launch
            }

            currentUserId = user.id
            notificationRepository.observeNotifications(user.id).collect { notifications ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notifications = notifications,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Khong cap nhat duoc thong bao.")
                    }
                }
        }
    }

    fun markAllAsRead() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Khong cap nhat duoc thong bao.")
                    }
                }
        }
    }
}

