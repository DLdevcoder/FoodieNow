package com.example.foodienow.domain.model

enum class UserRole {
    CUSTOMER,
    MERCHANT,
    SHIPPER
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val token: String = "" // Dùng để xác thực API sau này
)