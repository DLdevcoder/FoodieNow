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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationFilter {
    ALL, UNREAD
}

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
    val errorMessage: String? = null,
    val filterType: NotificationFilter = NotificationFilter.ALL,
    val deletedIds: Set<String> = emptySet(),
    val readIds: Set<String> = emptySet()
) {
    val processedNotifications: List<AppNotification>
        get() = notifications
            .filter { it.id !in deletedIds }
            .map {
                if (it.id in readIds) it.copy(isRead = true) else it
            }

    val unreadCount: Int = processedNotifications.count { !it.isRead }
    
    val filteredNotifications: List<AppNotification>
        get() = if (filterType == NotificationFilter.UNREAD) {
            processedNotifications.filter { !it.isRead }
        } else {
            processedNotifications
        }
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var observeJob: Job? = null

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = authRepository.getAuthState().first()
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notifications = emptyList(),
                        errorMessage = "Không tìm thấy phiên đăng nhập."
                    )
                }
                return@launch
            }

            currentUserId = user.id
            notificationRepository.observeNotifications(user.id)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Không tải được thông báo. Vui lòng thử lại sau."
                        )
                    }
                }
                .collect { notifications ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            notifications = notifications,
                            errorMessage = null,
                            readIds = state.readIds.filter { id ->
                                notifications.any { it.id == id && !it.isRead }
                            }.toSet(),
                            deletedIds = state.deletedIds.filter { id ->
                                notifications.any { it.id == id }
                            }.toSet()
                        )
                    }
                }
        }
    }

    fun setFilter(filter: NotificationFilter) {
        _uiState.update { it.copy(filterType = filter) }
    }

    fun markAsRead(id: String) {
        _uiState.update { state ->
            state.copy(readIds = state.readIds + id)
        }
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        val userId = currentUserId ?: return
        val unreadIds = _uiState.value.notifications.filter { !it.isRead }.mapNotNull { it.id }
        _uiState.update { state ->
            state.copy(readIds = state.readIds + unreadIds)
        }
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
        }
    }

    fun deleteNotification(id: String) {
        _uiState.update { state ->
            state.copy(deletedIds = state.deletedIds + id)
        }
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
        }
    }
}

