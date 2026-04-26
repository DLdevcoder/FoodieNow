package com.example.foodienow.feature.notification

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestampLabel: String,
    val isRead: Boolean
)

data class NotificationUiState(
    val notifications: List<AppNotification> = emptyList()
) {
    val unreadCount: Int = notifications.count { !it.isRead }
}

class NotificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        NotificationUiState(
            notifications = listOf(
                AppNotification(
                    id = "n1",
                    title = "Don hang moi",
                    message = "Don hang #FN2401 da duoc tao thanh cong.",
                    timestampLabel = "Vua xong",
                    isRead = false
                ),
                AppNotification(
                    id = "n2",
                    title = "Cap nhat giao hang",
                    message = "Shipper dang tren duong giao don #FN2398.",
                    timestampLabel = "10 phut truoc",
                    isRead = false
                ),
                AppNotification(
                    id = "n3",
                    title = "Khuyen mai",
                    message = "Giam 20% cho don tu 120.000 VND hom nay.",
                    timestampLabel = "Hom qua",
                    isRead = true
                )
            )
        )
    )
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun markAsRead(id: String) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { item ->
                    if (item.id == id) item.copy(isRead = true) else item
                }
            )
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
        }
    }
}

