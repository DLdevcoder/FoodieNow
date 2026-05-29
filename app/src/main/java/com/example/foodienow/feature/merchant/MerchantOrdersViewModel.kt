package com.example.foodienow.feature.merchant

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

data class MerchantOrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val error: String? = null,
    val currentMerchantId: String? = null
)

@HiltViewModel
class MerchantOrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MerchantOrdersUiState())
    val uiState: StateFlow<MerchantOrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()
                val merchantId = currentUser?.id

                if (merchantId != null) {
                    // Lưu lại ID để dùng lúc xác nhận đơn
                    _uiState.update { it.copy(currentMerchantId = merchantId) }

                    orderRepository.getMerchantOrders(merchantId).collect { orderList ->
                        val sortedOrders = orderList.sortedByDescending { it.createdAt }
                        _uiState.update { it.copy(isLoading = false, orders = sortedOrders) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy thông tin định danh chủ quán") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            val merchantId = _uiState.value.currentMerchantId ?: return@launch

            _uiState.update { currentState ->
                val updatedOrders = currentState.orders.map { order ->
                    if (order.id == orderId) order.copy(status = OrderStatus.PREPARING) else order
                }
                currentState.copy(orders = updatedOrders)
            }

            val result = orderRepository.merchantAcceptOrderWithLocation(orderId, merchantId)

            if (result.isFailure) {
                loadOrders()
            }
        }
    }

    fun rejectOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedOrders = currentState.orders.map { order ->
                    if (order.id == orderId) order.copy(status = OrderStatus.CANCELLED_BY_STORE) else order
                }
                currentState.copy(orders = updatedOrders)
            }

            val result = orderRepository.cancelOrderWithReason(orderId, reason, "MERCHANT")
            if (result.isFailure) {
                loadOrders()
            }
        }
    }

    fun markOrderReady(orderId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedOrders = currentState.orders.map { order ->
                    if (order.id == orderId) order.copy(status = OrderStatus.WAITING_SHIPPER) else order
                }
                currentState.copy(orders = updatedOrders)
            }

            val result = orderRepository.storeMarkReady(orderId)
            if (result.isFailure) {
                loadOrders()
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedOrders = currentState.orders.map { order ->
                    if (order.id == orderId) order.copy(status = newStatus) else order
                }
                currentState.copy(orders = updatedOrders)
            }

            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            if (result.isFailure) {
                loadOrders()
            }
        }
    }
}