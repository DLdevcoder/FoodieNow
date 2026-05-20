package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatSummary(
    @SerialName("store_id") val storeId: String,
    @SerialName("partner_id") val partnerId: String,
    @SerialName("partner_name") val partnerName: String,
    @SerialName("partner_avatar") val partnerAvatar: String? = null,
    @SerialName("last_message") val lastMessage: String,
    @SerialName("last_message_time") val lastMessageTime: String,
    @SerialName("unread_count") val unreadCount: Int
)