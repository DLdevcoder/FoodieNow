package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.ReviewUiModel

interface ReviewRepository {
    suspend fun getReviewsByFoodId(foodId: String): List<ReviewUiModel>
}