package com.example.foodienow.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderRepository // Dùng chung Repo xử lý Order
) : ViewModel() {

    // Input: Món ăn khách chọn
    fun addToCart(food: Food, quantity: Int) {
        // Xử lý logic thêm vào danh sách giỏ hàng local
    }

    // Input: Danh sách món trong giỏ
    fun checkout() {
        viewModelScope.launch {
            // Chuyển CartItem thành Order và gọi API tạo đơn
            // Output: Đơn hàng mới có status PENDING bắn về cho Merchant
        }
    }
}