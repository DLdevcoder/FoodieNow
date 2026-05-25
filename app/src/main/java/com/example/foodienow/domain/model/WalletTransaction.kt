package com.example.foodienow.domain.model

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
enum class WalletTransactionType {
    TOP_UP,
    PAYMENT,
    WITHDRAW,
    REFUND
}

@Serializable
data class WalletTransaction(
    val id: String,
    val type: WalletTransactionType,
    val amount: Long,
    val description: String,
    @SerialName("created_at") val createdAt: String
)
