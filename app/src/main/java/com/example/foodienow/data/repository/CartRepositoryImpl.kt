package com.example.foodienow.data.repository

import com.example.foodienow.data.local.room.CartDao
import com.example.foodienow.data.local.room.CartEntity
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override val cartItems: Flow<Map<Food, Int>> = cartDao.getAllCartItems().map { list ->
        list.associate { it.toFood() to it.quantity }
    }

    override suspend fun addToCart(food: Food, quantity: Int) = withContext(Dispatchers.IO) {
        val existingItem = cartDao.getCartItemById(food.id)
        val newQuantity = (existingItem?.quantity ?: 0) + quantity
        cartDao.insertCartItem(CartEntity.fromFood(food, newQuantity))
    }

    override suspend fun updateQuantity(food: Food, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(food.id)
        } else {
            cartDao.insertCartItem(CartEntity.fromFood(food, quantity))
        }
    }

    override suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }
}