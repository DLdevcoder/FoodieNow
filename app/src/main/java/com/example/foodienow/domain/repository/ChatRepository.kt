package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.ChatSummary
import com.example.foodienow.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // Lấy lịch sử chat giữa 2 người trong 1 cửa hàng
    suspend fun getChatHistory(storeId: String, user1: String, user2: String): List<Message>

    // Gửi tin nhắn mới
    suspend fun sendMessage(message: Message): Boolean

    // Lắng nghe tin nhắn realtime
    fun listenToMessages(storeId: String): Flow<Message>

    suspend fun getChatSummaries(userId: String): List<ChatSummary>
}