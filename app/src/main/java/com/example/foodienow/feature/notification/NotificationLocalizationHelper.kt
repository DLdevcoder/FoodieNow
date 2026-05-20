package com.example.foodienow.feature.notification

import android.content.Context
import com.example.foodienow.R
import com.example.foodienow.domain.model.AppNotification
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object NotificationLocalizationHelper {
    
    fun getLocalizedTitleAndBody(context: Context, titleKey: String, messagePayload: String): Pair<String, String> {
        return try {
            val json = Json.parseToJsonElement(messagePayload).jsonObject
            val type = json["type"]?.jsonPrimitive?.content ?: ""

            when (type) {
                "NEW_ORDER" -> {
                    val price = json["total_price"]?.jsonPrimitive?.content ?: ""
                    Pair(
                        context.getString(R.string.notification_title_order_new),
                        context.getString(R.string.notification_body_order_new, price)
                    )
                }
                "ORDER_PREPARING" -> {
                    Pair(
                        context.getString(R.string.notification_title_order_preparing),
                        context.getString(R.string.notification_body_order_preparing)
                    )
                }
                "ORDER_DELIVERING" -> {
                    Pair(
                        context.getString(R.string.notification_title_order_delivering),
                        context.getString(R.string.notification_body_order_delivering)
                    )
                }
                "ORDER_COMPLETED" -> {
                    Pair(
                        context.getString(R.string.notification_title_order_completed),
                        context.getString(R.string.notification_body_order_completed)
                    )
                }
                "ORDER_CANCELLED" -> {
                    Pair(
                        context.getString(R.string.notification_title_order_cancelled),
                        context.getString(R.string.notification_body_order_cancelled)
                    )
                }
                "NEW_REVIEW" -> {
                    val rating = json["rating"]?.jsonPrimitive?.content ?: ""
                    val foodName = json["food_name"]?.jsonPrimitive?.content ?: ""
                    Pair(
                        context.getString(R.string.notification_title_new_review),
                        context.getString(R.string.notification_body_new_review, rating, foodName)
                    )
                }
                "WALLET_TRANSACTION" -> {
                    val desc = json["description"]?.jsonPrimitive?.content ?: ""
                    Pair(
                        context.getString(R.string.notification_title_wallet_transaction),
                        context.getString(R.string.notification_body_wallet_transaction, desc)
                    )
                }
                "PAYMENT_SUCCESS" -> {
                    val orderId = json["order_id"]?.jsonPrimitive?.content ?: ""
                    val points = json["earned_points"]?.jsonPrimitive?.content ?: "0"
                    Pair(
                        context.getString(R.string.notification_title_payment_success),
                        context.getString(R.string.notification_body_payment_success, orderId, points)
                    )
                }
                else -> {
                    Pair(titleKey, messagePayload)
                }
            }
        } catch (e: Exception) {
            Pair(titleKey, messagePayload)
        }
    }

    fun getLocalizedNotification(context: Context, notification: AppNotification): Pair<String, String> {
        return getLocalizedTitleAndBody(context, notification.title, notification.message)
    }
}
