package com.example.foodienow.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val cartItems: Map<Food, Int> = emptyMap(),
    val isLoading: Boolean = false
)

sealed class CartEvent {
    object NavigateToLogin : CartEvent()
    data class NavigateToCheckout(val userId: String, val cartItems: Map<Food, Int>) : CartEvent()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    // Tự động gộp dữ liệu từ CartRepository và trạng thái Loading thành UiState
    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.cartItems,
        _isLoading
    ) { items, loading ->
        CartUiState(
            cartItems = items,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CartUiState()
    )

    private val _cartEvent = MutableSharedFlow<CartEvent>()
    val cartEvent = _cartEvent.asSharedFlow()

    // Chuyển việc lưu trữ cho Repository xử lý để đồng bộ toàn app
    fun addToCart(food: Food, quantity: Int) {
        viewModelScope.launch {
            cartRepository.addToCart(food, quantity)
        }
    }

    fun updateQuantity(food: Food, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(food, quantity)
        }
    }

    // Xử lý khi bấm nút Đặt hàng
    fun onCheckoutClicked() {
        viewModelScope.launch {
            _isLoading.value = true

            val currentUser = authRepository.getAuthState().first()

            _isLoading.value = false

            if (currentUser == null) {
                _cartEvent.emit(CartEvent.NavigateToLogin)
            } else {
                _cartEvent.emit(CartEvent.NavigateToCheckout(currentUser.id, uiState.value.cartItems))
            }
        }
    }
}