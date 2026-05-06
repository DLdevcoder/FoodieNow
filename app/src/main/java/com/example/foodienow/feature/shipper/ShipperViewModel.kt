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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShipperUiState(
    val availableOrders: List<Order> = emptyList(),
    val activeOrder: Order? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ShipperViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipperUiState())
    val uiState: StateFlow<ShipperUiState> = _uiState.asStateFlow()

    private var shipperId: String? = null

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = authRepository.getAuthState().first()
            shipperId = user?.id

            if (shipperId != null) {
                // Subscribe to available deliveries
                launch {
                    orderRepository.getAvailableDeliveries().collect { orders ->
                        _uiState.update { state ->
                            state.copy(
                                availableOrders = orders,
                                isLoading = false
                            )
                        }
                    }
                }
                
                // Subscribe to active order
                launch {
                    orderRepository.getShipperActiveOrder(shipperId!!).collect { active ->
                        _uiState.update { state ->
                            state.copy(
                                activeOrder = active,
                                isLoading = false
                            )
                        }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Not logged in") }
            }
        }
    }

    fun acceptOrder(orderId: String) {
        val currentShipperId = shipperId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = orderRepository.acceptOrder(orderId, currentShipperId)
            result.onSuccess {
                // Realtime will update the list
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = orderRepository.updateOrderStatus(orderId, OrderStatus.COMPLETED)
            result.onSuccess {
                _uiState.update { it.copy(activeOrder = null, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
