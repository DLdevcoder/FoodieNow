package com.example.foodienow.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.R
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.model.Review
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.model.WalletTransactionType
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.OrderRepository
import com.example.foodienow.domain.repository.PaymentRepository
import com.example.foodienow.domain.repository.ReviewRepository
import com.example.foodienow.data.repository.MockWalletTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

enum class ActivityType {
    ORDER,
    PAYMENT,
    REVIEW,
    WALLET_TRANSACTION
}

data class ActivityHistoryItem(
    val id: String,
    val type: ActivityType,
    val orderId: String? = null,
    val paymentId: String? = null,
    val status: String? = null,
    val method: PaymentMethod? = null,
    val provider: WalletProvider? = null,
    val totalPrice: Long? = null,
    val createdAt: String?,
    val rating: Int? = null,
    val comment: String? = null,
    val foodName: String? = null,
    val amount: Long? = null,
    val description: String? = null,
    val transactionType: WalletTransactionType? = null
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
    private val paymentRepository: PaymentRepository,
    private val reviewRepository: ReviewRepository,
    private val walletTransactionRepository: MockWalletTransactionRepository
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
                paymentRepository.getPaymentsByCustomer(user.id),
                reviewRepository.getReviewsByCustomer(user.id),
                walletTransactionRepository.getTransactions(user.id)
            ) { orders, payments, reviews, transactions ->
                val orderItems = orders.map { it.toHistoryItem() }
                val paymentItems = payments.map { it.toHistoryItem() }
                val reviewItems = reviews.map { it.toHistoryItem() }
                val transactionItems = transactions.map { it.toHistoryItem() }
                (orderItems + paymentItems + reviewItems + transactionItems)
                    .sortedByDescending { it.createdAt.toSortableTime() }
            }.catch { e ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorResId = R.string.error_load_activity_history
                    )
                }
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

    private fun Review.toHistoryItem(): ActivityHistoryItem {
        return ActivityHistoryItem(
            id = "review-$id",
            type = ActivityType.REVIEW,
            orderId = orderId,
            rating = rating,
            comment = comment,
            foodName = foodName,
            createdAt = createdAt
        )
    }

    private fun WalletTransaction.toHistoryItem(): ActivityHistoryItem {
        return ActivityHistoryItem(
            id = "wallet-tx-$id",
            type = ActivityType.WALLET_TRANSACTION,
            amount = amount,
            description = description,
            transactionType = type,
            createdAt = createdAt
        )
    }

    private fun String?.toSortableTime(): Long {
        return runCatching {
            if (this.isNullOrBlank()) 0L else Instant.parse(this).toEpochMilli()
        }.getOrDefault(0L)
    }
}
