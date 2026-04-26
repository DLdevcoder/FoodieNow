package com.example.foodienow.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.OrderRepository
import com.example.foodienow.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

enum class ActivityType {
    ORDER,
    PAYMENT
}

data class ActivityHistoryItem(
    val id: String,
    val type: ActivityType,
    val title: String,
    val subtitle: String,
    val createdAt: String?
)

data class ActivityHistoryUiState(
    val isLoading: Boolean = true,
    val items: List<ActivityHistoryItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityHistoryUiState())
    val uiState: StateFlow<ActivityHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = authRepository.getAuthState().first()
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = emptyList(),
                        errorMessage = "Khong tim thay phien dang nhap."
                    )
                }
                return@launch
            }

            combine(
                orderRepository.getOrdersByCustomer(user.id),
                paymentRepository.getPaymentsByCustomer(user.id)
            ) { orders, payments ->
                (orders.map { it.toHistoryItem() } + payments.map { it.toHistoryItem() })
                    .sortedByDescending { it.createdAt.toSortableTime() }
            }.collect { items ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = items,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun Order.toHistoryItem(): ActivityHistoryItem {
        val orderId = id ?: "-"
        return ActivityHistoryItem(
            id = "order-$orderId",
            type = ActivityType.ORDER,
            title = "Don hang #$orderId",
            subtitle = "Trang thai: ${status.name} | Tong: ${"%.0f".format(totalPrice)} VND",
            createdAt = createdAt
        )
    }

    private fun Payment.toHistoryItem(): ActivityHistoryItem {
        val paymentId = id ?: "-"
        val linkedOrder = orderId ?: "-"
        return ActivityHistoryItem(
            id = "payment-$paymentId",
            type = ActivityType.PAYMENT,
            title = "Thanh toan #$paymentId",
            subtitle = "Don: #$linkedOrder | ${method.name} | ${status.name}",
            createdAt = createdAt
        )
    }

    private fun String?.toSortableTime(): Long {
        return runCatching {
            if (this.isNullOrBlank()) 0L else Instant.parse(this).toEpochMilli()
        }.getOrDefault(0L)
    }
}

