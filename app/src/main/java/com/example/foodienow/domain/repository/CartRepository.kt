package com.example.foodienow.domain.repository

import com.example.foodienow.data.local.room.CartDao
import com.example.foodienow.data.local.room.CartEntity
import com.example.foodienow.domain.model.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val cartDao: CartDao
) {
    val cartItems: Flow<Map<Food, Int>> = cartDao.getAllCartItems().map { list ->
        list.associate { it.toFood() to it.quantity }
    }

    suspend fun addToCart(food: Food, quantity: Int) = withContext(Dispatchers.IO) {
        val currentStoreId = cartDao.getCurrentStoreId()
        if (currentStoreId != null && currentStoreId != food.storeId) {
            cartDao.clearCart()
        }
        
        val existingItem = cartDao.getCartItemById(food.id)
        val newQuantity = (existingItem?.quantity ?: 0) + quantity
        cartDao.insertCartItem(CartEntity.fromFood(food, newQuantity))
    }

    suspend fun updateQuantity(food: Food, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(food.id)
        } else {
            cartDao.insertCartItem(CartEntity.fromFood(food, quantity))
        }
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }
}