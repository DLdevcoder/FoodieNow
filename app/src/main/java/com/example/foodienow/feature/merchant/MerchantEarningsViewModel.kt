package com.example.foodienow.feature.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class MerchantTransaction(
    val id: String,
    val title: String,
    val amount: Long,
    val date: Date,
    val isIncome: Boolean
)

data class MerchantEarningsUiState(
    val isLoading: Boolean = true,
    val currentBalance: Long = 0L,
    val todayEarnings: Long = 0L,
    val weekEarnings: Long = 0L,
    val recentTransactions: List<MerchantTransaction> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MerchantEarningsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MerchantEarningsUiState())
    val uiState: StateFlow<MerchantEarningsUiState> = _uiState.asStateFlow()

    init {
        loadEarningsData()
    }

    private fun loadEarningsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()
                val merchantId = currentUser?.id

                if (merchantId != null) {
                    orderRepository.getMerchantOrders(merchantId).collect { orders ->
                        val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED }
                        val transactions = completedOrders.map { order ->
                            MerchantTransaction(
                                id = order.id ?: "",
                                title = "Đơn hàng #${order.id?.take(8)}",
                                amount = order.totalPrice,
                                date = parseSupabaseDate(order.createdAt),
                                isIncome = true
                            )
                        }.sortedByDescending { it.date }

                        val todayStart = getStartOfToday()
                        val weekStart = getStartOfWeek()

                        val todayEarnings = transactions
                            .filter { it.date.after(todayStart) || it.date == todayStart }
                            .sumOf { it.amount }

                        val weekEarnings = transactions
                            .filter { it.date.after(weekStart) || it.date == weekStart }
                            .sumOf { it.amount }

                        val currentBalance = transactions.sumOf { it.amount }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentBalance = currentBalance,
                                todayEarnings = todayEarnings,
                                weekEarnings = weekEarnings,
                                recentTransactions = transactions
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Không tìm thấy thông tin tài khoản")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    private fun parseSupabaseDate(dateString: String?): Date {
        if (dateString.isNullOrBlank()) return Date()
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    private fun getStartOfToday(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getStartOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
