package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.model.WalletTransactionType
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.domain.payment.WalletPaymentGateway
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.data.repository.MockWalletTransactionRepository
import com.example.foodienow.data.repository.PaymentSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class WalletUiState(
    val balance: Long = 0L,
    val isProcessing: Boolean = false,
    val transactions: List<WalletTransaction> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val userRole: UserRole? = null,
    val linkedWallets: List<String> = emptyList()
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletPaymentGateway: WalletPaymentGateway,
    private val walletTransactionRepository: MockWalletTransactionRepository,
    private val paymentSettingsRepository: PaymentSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadBalanceAndRole()
        loadTransactions()
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
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            walletTransactionRepository.getTransactions().collect { list ->
                _uiState.update { it.copy(transactions = list) }
            }
        }
    }

    private fun loadBalanceAndRole() {
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(balance = user.balance, userRole = user.role) }
                }
            }
        }
    }

    fun topUp(amount: Long, provider: WalletProvider) {
        if (amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Số tiền nạp không hợp lệ.", successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }
            
            val user = authRepository.getAuthState().firstOrNull()
            if (user == null) {
                _uiState.update { it.copy(isProcessing = false, errorMessage = "Vui lòng đăng nhập lại.") }
                return@launch
            }

            val chargeResult = walletPaymentGateway.charge(
                provider = provider,
                amount = amount,
                orderId = "TOPUP-${System.currentTimeMillis()}",
                customerId = user.id
            )

            chargeResult.onSuccess {
                val updateResult = authRepository.updateBalance(amount)
                updateResult.onSuccess { updatedUser ->
                    walletTransactionRepository.addTransaction(
                        WalletTransaction(
                            id = "TXN-${System.currentTimeMillis()}",
                            type = WalletTransactionType.TOP_UP,
                            amount = amount,
                            description = "Nạp tiền từ ${provider.name}",
                            createdAt = Instant.now().toString()
                        )
                    )

                    _uiState.update { 
                        it.copy(
                            balance = updatedUser.balance,
                            isProcessing = false,
                            successMessage = "Nạp tiền thành công từ ${provider.name}."
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Nạp tiền thành công nhưng lỗi cập nhật số dư: ${error.message}"
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Giao dịch nạp tiền thất bại: ${error.message}"
                    )
                }
            }
        }
    }

    fun withdraw(amount: Long, providerName: String) {
        if (amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Số tiền rút không hợp lệ.", successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }

            val user = authRepository.getAuthState().firstOrNull()
            if (user == null) {
                _uiState.update { it.copy(isProcessing = false, errorMessage = "Vui lòng đăng nhập lại.") }
                return@launch
            }

            if (amount > user.balance) {
                _uiState.update { it.copy(isProcessing = false, errorMessage = "Số dư không đủ để thực hiện rút tiền.") }
                return@launch
            }

            val updateResult = authRepository.updateBalance(-amount)
            updateResult.onSuccess { updatedUser ->
                walletTransactionRepository.addTransaction(
                    WalletTransaction(
                        id = "TXN-${System.currentTimeMillis()}",
                        type = WalletTransactionType.WITHDRAW,
                        amount = amount,
                        description = "Rút tiền về ví $providerName",
                        createdAt = Instant.now().toString()
                    )
                )

                _uiState.update {
                    it.copy(
                        balance = updatedUser.balance,
                        isProcessing = false,
                        successMessage = "Rút tiền thành công về ví $providerName."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Rút tiền thất bại: ${error.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

