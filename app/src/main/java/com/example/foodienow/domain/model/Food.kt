package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Food(
    val id: String,
    @SerialName("merchant_id") val merchantId: String,
    val name: String,
    val description: String?,
    val price: Double,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("is_available") val isAvailable: Boolean = true
)