package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface MerchantRepository {
    suspend fun addFood(food: Food): Result<Unit>

    suspend fun updateFood(food: Food): Result<Unit>

    fun getMerchantMenu(merchantId: String): Flow<List<Food>>
    suspend fun getStoreById(storeId: String): Store
}