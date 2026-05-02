package com.example.foodienow.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.R
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
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
    val orderId: String? = null,
    val paymentId: String? = null,
    val status: String? = null,
    val method: PaymentMethod? = null,
    val provider: WalletProvider? = null,
    val totalPrice: Double? = null,
    val createdAt: String?
)

data class ActivityHistoryUiState(
    val isLoading: Boolean = true,
    val items: List<ActivityHistoryItem> = emptyList(),
    val errorResId: Int? = null
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
            _uiState.update { it.copy(isLoading = true, errorResId = null) }
            val user = authRepository.getAuthState().first()
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = emptyList(),
                        errorResId = R.string.error_no_session
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
                        errorResId = null
                    )
                }
            }
        }
    }

    private fun Order.toHistoryItem(): ActivityHistoryItem {
        val orderId = id
        return ActivityHistoryItem(
            id = "order-${orderId ?: "-"}",
            type = ActivityType.ORDER,
            orderId = orderId,
            status = status.name,
            totalPrice = totalPrice,
            createdAt = createdAt
        )
    }

    private fun Payment.toHistoryItem(): ActivityHistoryItem {
        val paymentId = id
        return ActivityHistoryItem(
            id = "payment-${paymentId ?: "-"}",
            type = ActivityType.PAYMENT,
            orderId = orderId,
            paymentId = paymentId,
            status = status.name,
            method = method,
            provider = provider,
            totalPrice = amount,
            createdAt = createdAt
        )
    }

    private fun String?.toSortableTime(): Long {
        return runCatching {
            if (this.isNullOrBlank()) 0L else Instant.parse(this).toEpochMilli()
        }.getOrDefault(0L)
    }
}
