package com.example.foodienow.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AppNotificationTest {

    @Test
    fun serialization_isCorrect() {
        val notification = AppNotification(
            id = "test-id",
            userId = "user-id",
            title = "TXT_ORDER_DRIVER_ASSIGNED",
            message = "{\"type\":\"ORDER_DRIVER_ASSIGNED\"}",
            channel = "push_and_tab",
            isRead = false
        )

        val jsonString = Json.encodeToString(AppNotification.serializer(), notification)
        val deserialized = Json.decodeFromString(AppNotification.serializer(), jsonString)

        assertEquals("test-id", deserialized.id)
        assertEquals("user-id", deserialized.userId)
        assertEquals("TXT_ORDER_DRIVER_ASSIGNED", deserialized.title)
        assertEquals("{\"type\":\"ORDER_DRIVER_ASSIGNED\"}", deserialized.message)
        assertEquals("push_and_tab", deserialized.channel)
        assertEquals(false, deserialized.isRead)
    }

    @Test
    fun deserialization_usesDefaultChannelIfMissing() {
        val jsonString = """
            {
                "id": "test-id",
                "user_id": "user-id",
                "title": "TXT_ORDER_PREPARING",
                "message": "{}",
                "is_read": false
            }
        """.trimIndent()

        val deserialized = Json.decodeFromString(AppNotification.serializer(), jsonString)

        assertEquals("push_and_tab", deserialized.channel)
    }

    @Test
    fun getDestinationRoute_resolvesRoutesCorrectly() {
        val notifPreparing = AppNotification(
            userId = "user-id",
            title = "TXT_ORDER_PREPARING",
            message = "{\"type\":\"ORDER_PREPARING\", \"order_id\":\"order-123\"}"
        )
        assertEquals("order_detail/order-123", com.example.foodienow.feature.notification.NotificationLocalizationHelper.getDestinationRoute(notifPreparing))

        val notifChat = AppNotification(
            userId = "user-id",
            title = "TXT_NEW_CHAT_MESSAGE",
            message = "{\"type\":\"NEW_CHAT_MESSAGE\", \"sender_id\":\"sender-123\", \"store_id\":\"store-123\", \"content\":\"Hi\"}"
        )
        assertEquals("chat/store-123/sender-123?title=Chat", com.example.foodienow.feature.notification.NotificationLocalizationHelper.getDestinationRoute(notifChat))

        val notifWallet = AppNotification(
            userId = "user-id",
            title = "TXT_WALLET_TRANSACTION",
            message = "{\"type\":\"WALLET_TRANSACTION\"}"
        )
        assertEquals("wallet_screen", com.example.foodienow.feature.notification.NotificationLocalizationHelper.getDestinationRoute(notifWallet))
    }
}
