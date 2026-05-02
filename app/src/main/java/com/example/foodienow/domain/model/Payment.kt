package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethod {
    COD,
    CARD,
    WALLET
}

@Serializable
enum class WalletProvider {
    ZALOPAY,
    MOMO
}

@Serializable
enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}

@Serializable
data class Payment(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    @SerialName("order_id") val orderId: String? = null,
    val amount: Double,
    val method: PaymentMethod,
    val provider: WalletProvider? = null,
    @SerialName("transaction_id") val transactionId: String? = null,
    val status: PaymentStatus = PaymentStatus.SUCCESS,
    @SerialName("delivery_address") val deliveryAddress: String,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

