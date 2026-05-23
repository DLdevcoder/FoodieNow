package com.example.foodienow.feature.shipper

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShipperUiState(
    val isLoading: Boolean = false,
    val availableOrders: List<Order> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val completedOrders: List<Order> = emptyList(),
    val currentShipperId: String? = null,
    val error: String? = null
)

@HiltViewModel
class ShipperViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipperUiState())
    val uiState: StateFlow<ShipperUiState> = _uiState.asStateFlow()

    init {
        loadShipperData()
    }

    private fun loadShipperData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()
                val shipperId = currentUser?.id

                if (shipperId != null) {
                    _uiState.update { it.copy(currentShipperId = shipperId) }

                    // Lắng nghe đơn chờ nhận (Ở Repository cần lọc status = PREPARING)
                    launch {
                        orderRepository.getAvailableDeliveries().collect { orders ->
                            _uiState.update { state ->
                                state.copy(availableOrders = orders.sortedBy { it.createdAt })
                            }
                        }
                    }

                    // Lắng nghe đơn đang giao (Repository cần lấy các đơn có status = DRIVER_ASSIGNED hoặc DELIVERING)
                    launch {
                        orderRepository.getShipperActiveOrder(shipperId).collect { order ->
                            _uiState.update { state ->
                                val activeList = listOfNotNull(order)
                                state.copy(
                                    activeOrders = activeList,
                                    isLoading = false
                                )
                            }
                        }
                    }

                    // Lắng nghe lịch sử đơn hàng
                    launch {
                        orderRepository.getShipperCompletedOrders(shipperId).collect { orders ->
                            _uiState.update { state ->
                                state.copy(
                                    completedOrders = orders.sortedByDescending { it.createdAt }
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy thông tin Shipper") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun acceptOrder(orderId: String) {
        val shipperId = _uiState.value.currentShipperId ?: return
        viewModelScope.launch {
            // 1. Gắn shipper_id vào đơn hàng
            val result = orderRepository.acceptOrder(orderId, shipperId)

            if (result.isSuccess) {
                // 2. Chuyển trạng thái sang DRIVER_ASSIGNED (Tài xế đang đến lấy)
                val statusResult = orderRepository.updateOrderStatus(orderId, OrderStatus.DRIVER_ASSIGNED)
                if (statusResult.isFailure) {
                    Log.e("ShipperApp", "Lỗi cập nhật trạng thái sau khi nhận đơn: ", statusResult.exceptionOrNull())
                }
            } else {
                Log.e("ShipperApp", "Lỗi nhận đơn: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Không thể nhận đơn. Vui lòng thử lại.") }
            }
        }
    }

    fun markAsDelivering(orderId: String) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, OrderStatus.DELIVERING)
            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi chuyển trạng thái đang giao: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Lỗi cập nhật trạng thái. Vui lòng thử lại.") }
            }
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, OrderStatus.COMPLETED)
            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi hoàn tất đơn: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Không thể hoàn thành đơn. Vui lòng thử lại.") }
            }
        }
    }
}