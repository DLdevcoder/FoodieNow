package com.example.foodienow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    CUSTOMER,
    MERCHANT,
    SHIPPER
}

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val balance: Long = 0L,
    val rewardPoints: Int = 0,
    val token: String = "" // Dùng để xác thực API sau này
)