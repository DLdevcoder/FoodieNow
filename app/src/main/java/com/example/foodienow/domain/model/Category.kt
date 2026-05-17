package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String? = null,
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null
)