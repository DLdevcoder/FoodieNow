package com.example.foodienow.domain.model

data class ReviewUiModel(
    val id: String,
    val userName: String,
    val userAvatarUrl: String?,
    val rating: Int,
    val comment: String,
    val date: String
)