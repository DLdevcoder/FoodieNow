package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>

    suspend fun createNotification(notification: AppNotification): Result<AppNotification>

    suspend fun markAsRead(notificationId: String): Result<Unit>

    suspend fun markAllAsRead(userId: String): Result<Unit>
}

