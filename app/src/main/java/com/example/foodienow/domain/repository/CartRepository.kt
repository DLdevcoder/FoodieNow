package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val cartItems: Flow<Map<Food, Int>>

    suspend fun addToCart(food: Food, quantity: Int)

    suspend fun updateQuantity(food: Food, quantity: Int)

    suspend fun clearCart()
}