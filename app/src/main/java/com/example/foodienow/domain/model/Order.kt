package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    PENDING,    // Chờ chủ quán xác nhận
    PREPARING,  // Đang chuẩn bị món
    DELIVERING, // Shipper đang giao
    COMPLETED,  // Hoàn thành
    CANCELLED   // Đã hủy
}

@Serializable
data class Order(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    @SerialName("merchant_id") val merchantId: String? = null,
    @SerialName("shipper_id") val shipperId: String? = null,
    @SerialName("total_price") val totalPrice: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    @SerialName("delivery_address") val deliveryAddress: String,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("preview_image_url") val previewImageUrl: String? = null,
    @SerialName("preview_food_name") val previewFoodName: String? = null
)