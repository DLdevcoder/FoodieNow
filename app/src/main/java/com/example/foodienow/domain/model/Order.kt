package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    WAITING_PAYMENT,
    WAITING_STORE_CONFIRMATION,
    PREPARING,
    WAITING_SHIPPER,
    DELIVERING,
    COMPLETED,
    CANCELLED_BY_CUSTOMER,
    CANCELLED_BY_STORE,
    NO_SHIPPER_FOUND,
    PAYMENT_FAILED,
    DELIVERY_TIMEOUT;

    val isTerminal: Boolean
        get() = this in listOf(
            COMPLETED,
            CANCELLED_BY_CUSTOMER,
            CANCELLED_BY_STORE,
            NO_SHIPPER_FOUND,
            PAYMENT_FAILED,
            DELIVERY_TIMEOUT
        )

    val isActive: Boolean
        get() = !isTerminal

    val canCustomerCancel: Boolean
        get() = this in listOf(
            WAITING_PAYMENT,
            WAITING_STORE_CONFIRMATION,
            PREPARING,
            WAITING_SHIPPER
        )

    val canMerchantCancel: Boolean
        get() = this in listOf(
            WAITING_STORE_CONFIRMATION,
            PREPARING
        )

    val displayNameVi: String
        get() = when (this) {
            WAITING_PAYMENT -> "Đợi thanh toán"
            WAITING_STORE_CONFIRMATION -> "Đợi cửa hàng xác nhận"
            PREPARING -> "Đang chuẩn bị"
            WAITING_SHIPPER -> "Chờ shipper"
            DELIVERING -> "Đang vận chuyển"
            COMPLETED -> "Hoàn thành"
            CANCELLED_BY_CUSTOMER -> "Khách hàng hủy"
            CANCELLED_BY_STORE -> "Cửa hàng hủy"
            NO_SHIPPER_FOUND -> "Không tìm được shipper"
            PAYMENT_FAILED -> "Thanh toán thất bại"
            DELIVERY_TIMEOUT -> "Quá thời gian giao"
        }
}

@Serializable
data class Order(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    @SerialName("merchant_id") val merchantId: String? = null,
    @SerialName("shipper_id") val shipperId: String? = null,
    @SerialName("total_price") val totalPrice: Long,
    val status: OrderStatus = OrderStatus.WAITING_STORE_CONFIRMATION,
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
    @SerialName("shipper_lng") val shipperLng: Double? = null,
    val otherItemsCount: Int? = null,
    @SerialName("shipper_confirmed") val shipperConfirmed: Boolean = false,
    @SerialName("customer_confirmed") val customerConfirmed: Boolean = false,
    @SerialName("cancelled_by") val cancelledBy: String? = null,
    @SerialName("cancellation_reason") val cancellationReason: String? = null
)