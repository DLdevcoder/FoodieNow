package com.example.foodienow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WalletTransactionType {
    TOP_UP,
    PAYMENT
}

@Serializable
data class WalletTransaction(
    val id: String,
    val type: WalletTransactionType,
    val amount: Long,
    val description: String,
    val createdAt: String
)
