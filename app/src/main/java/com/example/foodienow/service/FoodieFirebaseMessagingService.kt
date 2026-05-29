package com.example.foodienow.service

import com.example.foodienow.data.local.UiPreferencesDataStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class FoodieFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var uiPreferencesDataStore: UiPreferencesDataStore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val rawTitle = message.data["title"] ?: message.notification?.title ?: "FoodieNow"
        val rawBody = message.data["body"] ?: message.notification?.body ?: ""

        if (rawBody.isNotBlank()) {
            val isEnabled = runBlocking {
                uiPreferencesDataStore.uiPreferencesFlow.first().notificationsEnabled
            }
            if (!isEnabled) return

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
