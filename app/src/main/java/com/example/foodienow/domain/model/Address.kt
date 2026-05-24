package com.example.foodienow.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String = "",
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    val detail: String,
    @SerialName("is_default") val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class AddressInsert(
    @SerialName("user_id") val userId: String,
    val title: String,
    val detail: String,
    @SerialName("is_default") val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)
