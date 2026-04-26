package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String = "",
    @SerialName("order_id") val orderId: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("food_id") val foodId: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)