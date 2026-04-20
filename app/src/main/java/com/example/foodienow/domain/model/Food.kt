package com.example.foodienow.domain.model

@kotlinx.serialization.Serializable
data class Food(
    val id: String,
    val merchantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String // Đường dẫn ảnh
)