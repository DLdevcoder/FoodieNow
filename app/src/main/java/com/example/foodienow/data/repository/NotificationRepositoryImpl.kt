package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.AppNotification
import com.example.foodienow.domain.repository.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.time.Instant
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : NotificationRepository {

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> = flow {
        while (currentCoroutineContext().isActive) {
            val notifications = runCatching {
                supabaseClient.postgrest["notifications"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<AppNotification>()
                    .sortedByDescending { it.createdAt.orEmpty() }
            }.getOrDefault(emptyList())

            emit(notifications)
            // Polling keeps UI in sync until websocket realtime is wired.
            delay(POLL_INTERVAL_MILLIS)
        }
    }

    override suspend fun createNotification(notification: AppNotification): Result<AppNotification> {
        return try {
            val created = supabaseClient.postgrest["notifications"]
                .insert(notification) {
                    select()
                }
                .decodeSingle<AppNotification>()
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            val now = Instant.now().toString()
            supabaseClient.postgrest["notifications"].update(
                {
                    set("is_read", true)
                    set("read_at", now)
                }
            ) {
                filter {
                    eq("id", notificationId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val now = Instant.now().toString()
            supabaseClient.postgrest["notifications"].update(
                {
                    set("is_read", true)
                    set("read_at", now)
                }
            ) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 3000L
    }
}


