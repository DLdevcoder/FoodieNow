package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.ChatSummary
import com.example.foodienow.domain.model.Message
import com.example.foodienow.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ChatRepository {

    override suspend fun getChatHistory(storeId: String, user1: String, user2: String): List<Message> {
        return try {
            supabase.postgrest["messages"]
                .select {
                    filter {
                        eq("store_id", storeId)
                        isIn("sender_id", listOf(user1, user2))
                        isIn("receiver_id", listOf(user1, user2))
                    }
                    order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }.decodeList<Message>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun sendMessage(message: Message): Boolean {
        return try {
            supabase.postgrest["messages"].insert(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // SỬA LẠI HÀM NÀY: Dùng channelFlow để giữ kết nối Realtime
    override fun listenToMessages(storeId: String): Flow<Message> = channelFlow {
        val channelName = "chat_channel_$storeId"
        val channel = supabase.channel(channelName)

        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter = "store_id=eq.$storeId"
        }

        launch {
            changeFlow.collect { action ->
                try {
                    val message = action.decodeRecord<Message>()
                    send(message) // Dùng send() thay vì emit() trong channelFlow
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        channel.subscribe()

        // Hủy kênh khi người dùng thoát khỏi màn hình Chat
        awaitClose {
            launch {
                supabase.realtime.removeChannel(channel)
            }
        }
    }

    override suspend fun getChatSummaries(userId: String): List<ChatSummary> {
        return try {
            supabase.postgrest.rpc(
                function = "get_chat_summaries",
                parameters = mapOf("p_user_id" to userId)
            ).decodeList<ChatSummary>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun markMessagesAsRead(storeId: String, partnerId: String, currentUserId: String) {
        try {
            supabase.postgrest["messages"].update(
                mapOf("is_read" to true)
            ) {
                filter {
                    eq("store_id", storeId)
                    eq("sender_id", partnerId)
                    eq("receiver_id", currentUserId)
                    eq("is_read", false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun listenToUnreadCount(userId: String): Flow<Int> = channelFlow {
        var currentCount = 0
        try {
            val initialResult = supabase.postgrest["messages"].select(head = true) {
                filter {
                    eq("receiver_id", userId)
                    eq("is_read", false)
                }
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
            }
            currentCount = initialResult.countOrNull()?.toInt() ?: 0
            send(currentCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val channelName = "unread_count_channel_$userId"
        val channel = supabase.channel(channelName)
        val insertFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter = "receiver_id=eq.$userId"
        }
        val updateFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "messages"
            filter = "receiver_id=eq.$userId"
        }

        launch {
            insertFlow.collect { action ->
                try {
                    val message = action.decodeRecord<Message>()
                    if (message.receiverId == userId && !message.isRead) {
                        currentCount++
                        send(currentCount)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        launch {
            updateFlow.collect { action ->
                try {
                    val message = action.decodeRecord<Message>()
                    val oldRecord = action.oldRecord
                    val result = supabase.postgrest["messages"].select(head = true) {
                        filter {
                            eq("receiver_id", userId)
                            eq("is_read", false)
                        }
                        count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                    }
                    val newCount = result.countOrNull()?.toInt() ?: 0
                    if (newCount != currentCount) {
                        currentCount = newCount
                        send(currentCount)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        channel.subscribe()

        awaitClose {
            launch {
                supabase.realtime.removeChannel(channel)
            }
        }
    }

    override fun listenToAnyMessageChanges(userId: String): Flow<Unit> = channelFlow {
        val channelName = "chat_list_changes_$userId"
        val channel = supabase.channel(channelName)
        val receiveFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
            filter = "receiver_id=eq.$userId"
        }

        val sendFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
            filter = "sender_id=eq.$userId"
        }

        launch {
            receiveFlow.collect { send(Unit) }
        }

        launch {
            sendFlow.collect { send(Unit) }
        }

        channel.subscribe()

        awaitClose {
            launch { supabase.realtime.removeChannel(channel) }
        }
    }
}