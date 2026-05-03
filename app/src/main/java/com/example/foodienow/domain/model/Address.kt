package com.example.foodienow.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Address(
    val id: String = UUID.randomUUID().toString(),
    val title: String, // e.g. "Nhà riêng", "Công ty"
    val detail: String, // e.g. "Số 123 Đường ABC..."
    val isDefault: Boolean = false
)
