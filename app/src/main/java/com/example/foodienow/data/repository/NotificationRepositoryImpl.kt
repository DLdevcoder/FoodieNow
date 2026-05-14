package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.AppNotification
import com.example.foodienow.domain.repository.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : NotificationRepository {

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> = channelFlow {
        send(fetchNotifications(userId))

        val channel = supabaseClient.realtime.channel("notifications-$userId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
            filter = "user_id=eq.$userId"
        }

        launch {
            changeFlow.collect {
                send(fetchNotifications(userId))
            }
        }

        channel.subscribe()

        awaitClose {
            launch {
                channel.unsubscribe()
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    private suspend fun fetchNotifications(userId: String): List<AppNotification> {
        return runCatching {
            supabaseClient.postgrest["notifications"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<AppNotification>()
                .sortedByDescending { it.createdAt.orEmpty() }
        }.getOrDefault(emptyList())
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

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["notifications"].delete {
                filter {
                    eq("id", notificationId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
