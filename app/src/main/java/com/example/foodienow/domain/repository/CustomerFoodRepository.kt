package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface CustomerFoodRepository {
    // Hiển thị ở màn hình trang chủ
    fun getRecommendedFoods(): Flow<List<Food>>

    // Tìm kiếm món ăn
    fun searchFoods(query: String): Flow<List<Food>>

    suspend fun getFoodById(foodId: String): Food

    // Thêm hàm này để hết lỗi ở getFoodsByStoreId
    suspend fun getFoodsByStoreId(storeId: String): List<Food>
}