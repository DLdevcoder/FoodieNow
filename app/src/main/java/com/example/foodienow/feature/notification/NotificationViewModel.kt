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
    val filterType: NotificationFilter = NotificationFilter.ALL
) {
    val unreadCount: Int = notifications.count { !it.isRead }
    
    val filteredNotifications: List<AppNotification>
        get() = if (filterType == NotificationFilter.UNREAD) {
            notifications.filter { !it.isRead }
        } else {
            notifications
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

    fun setFilter(filter: NotificationFilter) {
        _uiState.update { it.copy(filterType = filter) }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
                // Ignore failure to prevent hiding the whole list with an error state
        }
    }

    fun markAllAsRead() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
        }
    }

    // Dành cho mục đích test Realtime
    fun sendTestNotification() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            val testNotifications = listOf(
                AppNotification(
                    userId = userId,
                    title = "Đơn hàng đã xác nhận",
                    message = "Nhà hàng đã nhận đơn và đang chuẩn bị món cho bạn.",
                    createdAt = java.time.Instant.now().toString()
                ),
                AppNotification(
                    userId = userId,
                    title = "Tài xế đang giao",
                    message = "Tài xế Nguyễn Văn A đang trên đường giao món đến bạn.",
                    createdAt = java.time.Instant.now().toString()
                ),
                AppNotification(
                    userId = userId,
                    title = "Nhà hàng hết món",
                    message = "Quán hiện đã hết Gà rán, vui lòng chọn món khác thay thế.",
                    createdAt = java.time.Instant.now().toString()
                ),
                AppNotification(
                    userId = userId,
                    title = "Giảm giá 30%",
                    message = "Voucher đặc biệt giảm 30% cho đơn hàng tiếp theo của bạn!",
                    createdAt = java.time.Instant.now().toString()
                )
            )
            // Lấy ngẫu nhiên 1 cái để test các style khác nhau
            val randomNotif = testNotifications.random()
            notificationRepository.createNotification(randomNotif)
        }
    }
}

