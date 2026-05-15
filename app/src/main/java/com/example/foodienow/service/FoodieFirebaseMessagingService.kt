package com.example.foodienow.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FoodieFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: message.notification?.title ?: "FoodieNow"
        val body = message.data["body"] ?: message.notification?.body ?: ""

        if (body.isNotBlank()) {
            NotificationHelper.showNotification(
                context = this,
                title = title,
                message = body
            )
        }
    }
}
