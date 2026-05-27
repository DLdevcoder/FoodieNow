package com.example.foodienow.feature.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.PaymentSettingsRepository
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.OrderRepository
import com.example.foodienow.domain.payment.WalletPaymentGateway
import com.example.foodienow.domain.model.WalletProvider
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

data class Transaction(
    val id: String,
    val title: String,
    val amount: Long,
    val date: Date,
    val isIncome: Boolean
)

data class ShipperEarningsUiState(
    val isLoading: Boolean = true,
    val currentBalance: Long = 0L,
    val todayEarnings: Long = 0L,
    val weekEarnings: Long = 0L,
    val recentTransactions: List<Transaction> = emptyList(),
    val error: String? = null,
    val linkedWallets: List<String> = emptyList(),
    val isWithdrawing: Boolean = false,
    val withdrawSuccess: Boolean = false,
    val lastWithdrawalAmount: Long = 0L,
    val lastWithdrawalWallet: String = ""
)

@HiltViewModel
class ShipperEarningsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val paymentSettingsRepository: PaymentSettingsRepository,
    private val walletPaymentGateway: WalletPaymentGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipperEarningsUiState())
    val uiState: StateFlow<ShipperEarningsUiState> = _uiState.asStateFlow()

    init {
        loadEarningsData()
        observePaymentSettings()
    }

    private fun observePaymentSettings() {
        viewModelScope.launch {
            paymentSettingsRepository.refreshSettings()
            paymentSettingsRepository.settings.collect { settings ->
                val wallets = settings.configuredOptionIds.filter {
                    it in setOf("momo", "zalopay", "vnpay", "paypal")
                }
                _uiState.update { it.copy(linkedWallets = wallets) }
            }
        }
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(currentBalance = user.balance) }
                }
            }
        }
    }

    private fun loadEarningsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()
                val shipperId = currentUser?.id

                if (shipperId != null) {
                    orderRepository.getShipperCompletedOrders(shipperId).collect { orders ->
                        val transactions = orders.map { order ->
                            Transaction(
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

                        _uiState.update {
                            it.copy(
                                isLoading = false,
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

    fun withdraw(amount: Long, walletId: String) {
        if (!_uiState.value.linkedWallets.contains(walletId)) {
            _uiState.update { it.copy(error = "Ví nhận tiền chưa được liên kết.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isWithdrawing = true, error = null) }
            
            val user = authRepository.getAuthState().firstOrNull()
            if (user == null) {
                _uiState.update { it.copy(isWithdrawing = false, error = "Vui lòng đăng nhập lại.") }
                return@launch
            }

            if (amount > user.balance) {
                _uiState.update { it.copy(isWithdrawing = false, error = "Số dư không đủ để thực hiện rút tiền.") }
                return@launch
            }

            val provider = when (walletId.lowercase()) {
                "momo" -> WalletProvider.MOMO
                "zalopay" -> WalletProvider.ZALOPAY
                "vnpay" -> WalletProvider.VNPAY
                "paypal" -> WalletProvider.PAYPAL
                else -> WalletProvider.MOMO
            }

            val withdrawResult = walletPaymentGateway.withdraw(
                provider = provider,
                amount = amount,
                transactionId = "TXN-${System.currentTimeMillis()}",
                customerId = user.id
            )

            withdrawResult.onSuccess { gatewayRes ->
                val walletName = when (walletId) {
                    "momo" -> "MoMo"
                    "zalopay" -> "ZaloPay"
                    "vnpay" -> "VNPAY"
                    "paypal" -> "PayPal"
                    else -> walletId.uppercase()
                }
                authRepository.updateBalance(-amount).onSuccess { updatedUser ->
                    _uiState.update { state ->
                        val newTransaction = Transaction(
                            id = gatewayRes.transactionId,
                            title = "Rút tiền về ví $walletName",
                            amount = amount,
                            date = Date(),
                            isIncome = false
                        )
                        state.copy(
                            recentTransactions = listOf(newTransaction) + state.recentTransactions,
                            isWithdrawing = false,
                            withdrawSuccess = true,
                            lastWithdrawalAmount = amount,
                            lastWithdrawalWallet = walletName,
                            currentBalance = updatedUser.balance
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isWithdrawing = false,
                            error = "Rút tiền thành công nhưng lỗi cập nhật số dư: ${error.message}"
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isWithdrawing = false,
                        error = "Giao dịch rút tiền thất bại: ${error.message}"
                    )
                }
            }
        }
    }

    fun resetWithdrawSuccess() {
        _uiState.update { it.copy(withdrawSuccess = false) }
    }
}