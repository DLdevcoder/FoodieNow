package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.ChatSummary
import com.example.foodienow.domain.model.Message
import com.example.foodienow.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.realtime.channel
import kotlinx.coroutines.flow.flow
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

    override fun listenToMessages(storeId: String): Flow<Message> = flow {
        // Dùng supabase.channel thay vì supabase.realtime.createChannel
        val channel = supabase.channel("chat_channel_$storeId")

        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter = "store_id=eq.$storeId"
        }

        channel.subscribe()

        changeFlow.collect { action ->
            try {
                val message = action.decodeRecord<Message>()
                emit(message)
            } catch (e: Exception) {
                e.printStackTrace()
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
                    eq("sender_id", partnerId) // Tin nhắn từ người kia gửi
                    eq("receiver_id", currentUserId) // Mình là người nhận
                    eq("is_read", false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getTotalUnreadCount(userId: String): Int {
        return try {
            val result = supabase.postgrest["messages"].select(head = true) {
                filter {
                    eq("receiver_id", userId)
                    eq("is_read", false)
                }
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
            }
            result.countOrNull()?.toInt() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}