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
    val balance: Long = 0L,
    @SerialName("reward_points") val rewardPoints: Int = 0,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("fcm_token") val fcmToken: String? = null
)

