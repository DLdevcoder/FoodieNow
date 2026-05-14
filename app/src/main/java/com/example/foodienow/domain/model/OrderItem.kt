package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItemResponse(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("food_id") val foodId: String,
    val quantity: Int,
    @SerialName("price_at_time") val priceAtTime: Long,
    val foods: FoodBasicInfo? = null
)

@Serializable
data class FoodBasicInfo(
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null
)

data class OrderItemUiModel(
    val id: String,
    val orderId: String,
    val foodId: String,
    val quantity: Int,
    val priceAtTime: Long,
    val foodName: String,
    val foodImageUrl: String?
)