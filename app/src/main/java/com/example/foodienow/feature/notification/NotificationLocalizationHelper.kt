package com.example.foodienow.feature.notification

import android.content.Context
import com.example.foodienow.R
import com.example.foodienow.domain.model.AppNotification
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object NotificationLocalizationHelper {
    
    fun getLocalizedTitleAndBody(context: Context, titleKey: String, messagePayload: String): Pair<String, String> {
        val result = try {
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
                "ORDER_DRIVER_ASSIGNED" -> {
                    Pair(
                        context.getString(R.string.notification_title_order_driver_assigned),
                        context.getString(R.string.notification_body_order_driver_assigned)
                    )
                }
                "SHIPPER_NEW_ORDER" -> {
                    Pair(
                        context.getString(R.string.notification_title_shipper_new_order),
                        context.getString(R.string.notification_body_shipper_new_order)
                    )
                }
                "ORDER_CANCELLED_SHIPPER" -> {
                    Pair(
                        context.getString(R.string.notification_title_order_cancelled_shipper),
                        context.getString(R.string.notification_body_order_cancelled_shipper)
                    )
                }
                "NEW_CHAT_MESSAGE" -> {
                    val content = json["content"]?.jsonPrimitive?.content ?: ""
                    Pair(
                        context.getString(R.string.notification_title_new_chat_message),
                        content
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
                    val points = json["earned_points"]?.jsonPrimitive?.content ?: "0"
                    Pair(
                        context.getString(R.string.notification_title_payment_success),
                        context.getString(R.string.notification_body_payment_success, points)
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }

        if (result != null) return result

        val defaultTitle = when (titleKey) {
            "TXT_ORDER_NEW" -> context.getString(R.string.notification_title_order_new)
            "TXT_ORDER_PREPARING" -> context.getString(R.string.notification_title_order_preparing)
            "TXT_ORDER_DRIVER_ASSIGNED" -> context.getString(R.string.notification_title_order_driver_assigned)
            "TXT_ORDER_DELIVERING" -> context.getString(R.string.notification_title_order_delivering)
            "TXT_ORDER_COMPLETED" -> context.getString(R.string.notification_title_order_completed)
            "TXT_ORDER_CANCELLED" -> context.getString(R.string.notification_title_order_cancelled)
            "TXT_ORDER_CANCELLED_SHIPPER" -> context.getString(R.string.notification_title_order_cancelled_shipper)
            "TXT_SHIPPER_NEW_ORDER" -> context.getString(R.string.notification_title_shipper_new_order)
            "TXT_NEW_CHAT_MESSAGE" -> context.getString(R.string.notification_title_new_chat_message)
            "TXT_NEW_REVIEW" -> context.getString(R.string.notification_title_new_review)
            "TXT_WALLET_TRANSACTION" -> context.getString(R.string.notification_title_wallet_transaction)
            "TXT_PAYMENT_SUCCESS" -> context.getString(R.string.notification_title_payment_success)
            else -> {
                if (titleKey.startsWith("TXT_")) {
                    context.getString(R.string.notifications_tab_title)
                } else {
                    titleKey
                }
            }
        }

        val defaultBody = when (titleKey) {
            "TXT_ORDER_PREPARING" -> context.getString(R.string.notification_body_order_preparing)
            "TXT_ORDER_DELIVERING" -> context.getString(R.string.notification_body_order_delivering)
            "TXT_ORDER_COMPLETED" -> context.getString(R.string.notification_body_order_completed)
            "TXT_ORDER_CANCELLED" -> context.getString(R.string.notification_body_order_cancelled)
            "TXT_ORDER_DRIVER_ASSIGNED" -> context.getString(R.string.notification_body_order_driver_assigned)
            "TXT_SHIPPER_NEW_ORDER" -> context.getString(R.string.notification_body_shipper_new_order)
            "TXT_ORDER_CANCELLED_SHIPPER" -> context.getString(R.string.notification_body_order_cancelled_shipper)
            else -> {
                try {
                    val json = Json.parseToJsonElement(messagePayload).jsonObject
                    when {
                        json.containsKey("description") -> json["description"]?.jsonPrimitive?.content ?: messagePayload
                        json.containsKey("content") -> json["content"]?.jsonPrimitive?.content ?: messagePayload
                        json.containsKey("order_id") -> {
                            if (json.containsKey("earned_points")) {
                                val points = json["earned_points"]?.jsonPrimitive?.content ?: "0"
                                context.getString(R.string.notification_body_payment_success, points)
                            } else {
                                context.getString(R.string.notification_body_order_updated)
                            }
                        }
                        else -> messagePayload
                    }
                } catch (e: Exception) {
                    messagePayload
                }
            }
        }

        return Pair(defaultTitle, defaultBody)
    }

    fun getLocalizedNotification(context: Context, notification: AppNotification): Pair<String, String> {
        return getLocalizedTitleAndBody(context, notification.title, notification.message)
    }

    fun getDestinationRoute(notification: AppNotification): String? {
        return try {
            val json = Json.parseToJsonElement(notification.message).jsonObject
            val type = json["type"]?.jsonPrimitive?.content ?: ""
            when (type) {
                "NEW_ORDER", "ORDER_PREPARING", "ORDER_DRIVER_ASSIGNED", "ORDER_DELIVERING", "ORDER_COMPLETED", "ORDER_CANCELLED", "ORDER_CANCELLED_SHIPPER" -> {
                    val orderId = json["order_id"]?.jsonPrimitive?.content
                    if (!orderId.isNullOrBlank()) "order_detail/$orderId" else null
                }
                "SHIPPER_NEW_ORDER" -> {
                    val orderId = json["order_id"]?.jsonPrimitive?.content
                    if (!orderId.isNullOrBlank()) "shipper_tracking/$orderId" else null
                }
                "NEW_REVIEW" -> {
                    val orderId = json["order_id"]?.jsonPrimitive?.content
                    if (!orderId.isNullOrBlank()) "order_detail/$orderId" else null
                }
                "NEW_CHAT_MESSAGE" -> {
                    val senderId = json["sender_id"]?.jsonPrimitive?.content
                    val storeId = json["store_id"]?.jsonPrimitive?.content
                    if (!senderId.isNullOrBlank() && !storeId.isNullOrBlank()) {
                        "chat/$storeId/$senderId?title=Chat"
                    } else null
                }
                "WALLET_TRANSACTION", "PAYMENT_SUCCESS" -> {
                    "wallet_screen"
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
