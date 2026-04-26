package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor() {
    private val _cartItems = MutableStateFlow<Map<Food, Int>>(emptyMap())
    val cartItems: StateFlow<Map<Food, Int>> = _cartItems.asStateFlow()

    fun addToCart(food: Food, quantity: Int) {
        _cartItems.update { currentCart ->
            val newCart = currentCart.toMutableMap()
            val existingQty = newCart[food] ?: 0
            newCart[food] = existingQty + quantity
            newCart
        }
    }

    fun updateQuantity(food: Food, quantity: Int) {
        _cartItems.update { currentCart ->
            val newCart = currentCart.toMutableMap()
            if (quantity <= 0) newCart.remove(food) else newCart[food] = quantity
            newCart
        }
    }
}