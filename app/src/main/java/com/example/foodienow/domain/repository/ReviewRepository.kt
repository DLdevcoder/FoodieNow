package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Review
import com.example.foodienow.domain.model.ReviewUiModel

interface ReviewRepository {
    suspend fun getReviewsByFoodId(foodId: String): List<ReviewUiModel>

    suspend fun getReviewByOrderAndFood(orderId: String, foodId: String): Review?

    suspend fun submitReview(
        orderId: String,
        customerId: String,
        foodId: String,
        rating: Int,
        comment: String
    ): Boolean

    suspend fun updateReview(
        reviewId: String,
        rating: Int,
        comment: String
    ): Boolean

    fun getReviewsByCustomer(customerId: String): kotlinx.coroutines.flow.Flow<List<Review>>
}