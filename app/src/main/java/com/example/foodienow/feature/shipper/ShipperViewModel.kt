package com.example.foodienow.feature.shipper

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

                    launch {
                        orderRepository.getAvailableDeliveries().collect { orders ->
                            _uiState.update { state ->
                                state.copy(availableOrders = orders.sortedBy { it.createdAt })
                            }
                        }
                    }

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
            val result = orderRepository.acceptOrder(orderId, shipperId)
            if (result.isFailure) {
                _uiState.update { it.copy(error = "Không thể nhận đơn. Vui lòng thử lại.") }
            }
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, OrderStatus.COMPLETED)
            if (result.isFailure) {
                _uiState.update { it.copy(error = "Không thể cập nhật trạng thái. Vui lòng thử lại.") }
            }
        }
    }
}