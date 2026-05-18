package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Food(
    val id: String = "",
    @SerialName("store_id") val storeId: String,
    val name: String,
    val description: String? = null,
    val price: Long,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_available") val isAvailable: Boolean = true,
    val rating: Double = 0.0,
    @SerialName("sold_count") val soldCount: Int = 0,
    @SerialName("category_id") val categoryId: String? = null
)