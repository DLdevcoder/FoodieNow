package com.example.foodienow.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FoodieFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val rawTitle = message.data["title"] ?: message.notification?.title ?: "FoodieNow"
        val rawBody = message.data["body"] ?: message.notification?.body ?: ""

        if (rawBody.isNotBlank()) {
            val (localizedTitle, localizedBody) = com.example.foodienow.feature.notification.NotificationLocalizationHelper.getLocalizedTitleAndBody(
                context = this,
                titleKey = rawTitle,
                messagePayload = rawBody
            )

            NotificationHelper.showNotification(
                context = this,
                title = localizedTitle,
                message = localizedBody
            )
        }
    }
}
