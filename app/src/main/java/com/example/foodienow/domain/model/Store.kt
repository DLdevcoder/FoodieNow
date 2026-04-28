package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: String = "",
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val address: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("opening_time") val openingTime: String? = null,
    @SerialName("closing_time") val closingTime: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val rating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0
)