package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    PENDING,          // 1. Khách mới đặt, chờ CHỦ QUÁN xác nhận. (Tài xế KHÔNG thấy đơn này).
    PREPARING,        // 2. Chủ quán ĐÃ XÁC NHẬN và đang nấu. Hệ thống bắt đầu đẩy đơn ra cho Tài xế.
    DRIVER_ASSIGNED,  // 3. Tài xế ĐÃ NHẬN ĐƠN và đang trên đường đến quán lấy đồ.
    DELIVERING,       // 4. Tài xế bấm "Đã lấy hàng" và đang trên đường giao cho khách.
    COMPLETED,        // 5. Giao hàng thành công.
    CANCELLED         // 6. Đơn bị hủy (bởi Khách, Chủ quán, hoặc Hệ thống do không tìm được tài xế).
}

@Serializable
data class Order(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    @SerialName("merchant_id") val merchantId: String? = null,
    @SerialName("shipper_id") val shipperId: String? = null,
    @SerialName("total_price") val totalPrice: Long,
    val status: OrderStatus = OrderStatus.PENDING,
    @SerialName("delivery_address") val deliveryAddress: String,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("preview_image_url") val previewImageUrl: String? = null,
    @SerialName("preview_food_name") val previewFoodName: String? = null,
    @SerialName("food_id") val foodId: String? = null,
    @SerialName("merchant_lat") val merchantLat: Double? = null,
    @SerialName("merchant_lng") val merchantLng: Double? = null,
    @SerialName("delivery_lat") val deliveryLat: Double? = null,
    @SerialName("delivery_lng") val deliveryLng: Double? = null,
    @SerialName("shipper_lat") val shipperLat: Double? = null,
    @SerialName("shipper_lng") val shipperLng: Double? = null
)