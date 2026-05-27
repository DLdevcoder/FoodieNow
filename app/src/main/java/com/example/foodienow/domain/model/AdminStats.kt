package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminFinancialStats(
    val date: String,
    @SerialName("total_orders") val totalOrders: Long,
    @SerialName("total_subtotal") val totalSubtotal: Long,
    @SerialName("total_delivery_fees") val totalDeliveryFees: Long,
    @SerialName("total_commissions") val totalCommissions: Long,
    @SerialName("total_merchant_payouts") val totalMerchantPayouts: Long,
    @SerialName("total_shipper_payouts") val totalShipperPayouts: Long
)

@Serializable
data class AdminAccountStats(
    val role: UserRole,
    @SerialName("total_users") val totalUsers: Long,
    @SerialName("total_balance") val totalBalance: Long
)

@Serializable
data class AdminProfileStats(
    val id: String,
    val email: String,
    @SerialName("full_name") val fullName: String,
    val role: UserRole,
    val balance: Long,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class AdminDetailedFinancialStats(
    @SerialName("total_system_balance") val totalSystemBalance: Long = 0L,
    @SerialName("pending_escrow_balance") val pendingEscrowBalance: Long = 0L,
    @SerialName("total_commissions") val totalCommissions: Long = 0L,
    @SerialName("total_shipper_balance") val totalShipperBalance: Long = 0L,
    @SerialName("total_merchant_balance") val totalMerchantBalance: Long = 0L
)

@Serializable
data class SystemSetting(
    val key: String,
    val value: Double
)
