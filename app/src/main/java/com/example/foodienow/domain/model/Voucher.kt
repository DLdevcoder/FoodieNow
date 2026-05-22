package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Voucher(
    val id: String,
    @SerialName("merchant_id") val merchantId: String?,
    val code: String,
    @SerialName("discount_percent") val discountPercent: Int,
    @SerialName("max_discount") val maxDiscount: Long,
    @SerialName("min_order_value") val minOrderValue: Long,
    @SerialName("discount_amount") val discountAmount: Long,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("max_usages_per_user") val maxUsagesPerUser: Int? = null,
    @SerialName("total_usages_limit") val totalUsagesLimit: Int? = null,
    @SerialName("starts_at") val startsAt: String? = null
)
