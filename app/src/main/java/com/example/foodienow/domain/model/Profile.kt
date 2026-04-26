package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val email: String,
    @SerialName("full_name") val fullName: String,
    val role: UserRole,
    val phone: String? = null,
    val address: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

