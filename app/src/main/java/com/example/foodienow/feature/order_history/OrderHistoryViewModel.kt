package com.example.foodienow.feature.order_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.R
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val customerFoodRepository: com.example.foodienow.domain.repository.CustomerFoodRepository,
    private val cartRepository: com.example.foodienow.domain.repository.CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collect { cartItems ->
                _uiState.update { it.copy(cartItems = cartItems) }
            }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorResId = null) }
            val user = authRepository.getAuthState().first()
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        orders = emptyList(),
                        errorResId = R.string.error_no_session
                    )
                }
                return@launch
            }

            try {
                orderRepository.getOrdersByCustomer(user.id).collect { orders ->
                    val sortedOrders = orders.sortedByDescending { it.createdAt.toSortableTime() }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orders = sortedOrders,
                            errorResId = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorResId = R.string.error_load_order_history
                    )
                }
            }
        }
    }

    fun reorder(orderId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Get items of the past order
                val orderItems = orderRepository.getOrderItemsByOrderId(orderId)
                
                // 2. Fetch each food and add to cart
                for (item in orderItems) {
                    try {
                        val food = customerFoodRepository.getFoodById(item.foodId)
                        cartRepository.addToCart(food, item.quantity)
                    } catch (e: Exception) {
                        // Ignore individual item failure or handle it
                        e.printStackTrace()
                    }
                }
                
                // 3. Trigger success callback
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorResId = R.string.error_load_order_history) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateQuantity(food: Food, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(food, quantity)
        }
    }

    private fun String?.toSortableTime(): Long {
        return runCatching {
            if (this.isNullOrBlank()) 0L else Instant.parse(this).toEpochMilli()
        }.getOrDefault(0L)
    }
}

data class OrderHistoryUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val cartItems: Map<Food, Int> = emptyMap(),
    val errorResId: Int? = null
)
