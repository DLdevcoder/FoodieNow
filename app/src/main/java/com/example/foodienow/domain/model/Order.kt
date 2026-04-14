package com.example.foodienow.domain.model

enum class OrderStatus {
    PENDING,    // Chờ chủ quán xác nhận
    PREPARING,  // Đang chuẩn bị món
    DELIVERING, // Shipper đang giao
    COMPLETED,  // Hoàn thành
    CANCELLED   // Đã hủy
}

// Định nghĩa 1 món trong giỏ hàng (Món ăn + Số lượng)
data class CartItem(
    val food: Food,
    val quantity: Int
)

// Toàn bộ thông tin của 1 đơn hàng
data class Order(
    val id: String,
    val customerId: String,
    val merchantId: String,
    val shipperId: String? = null,
    val items: List<CartItem>,
    val totalPrice: Double,
    val status: OrderStatus
)