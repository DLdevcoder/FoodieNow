package com.example.foodienow.feature.order_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.R
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
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
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

    private fun String?.toSortableTime(): Long {
        return runCatching {
            if (this.isNullOrBlank()) 0L else Instant.parse(this).toEpochMilli()
        }.getOrDefault(0L)
    }
}

data class OrderHistoryUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val errorResId: Int? = null
)
