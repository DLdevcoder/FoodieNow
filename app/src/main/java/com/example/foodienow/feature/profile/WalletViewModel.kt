package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.model.WalletTransactionType
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.WalletPaymentGateway
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.data.repository.MockWalletTransactionRepository
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
    val balance: Double = 0.0,
    val isProcessing: Boolean = false,
    val transactions: List<WalletTransaction> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletPaymentGateway: WalletPaymentGateway,
    private val walletTransactionRepository: MockWalletTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadBalance()
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            walletTransactionRepository.getTransactions().collect { list ->
                _uiState.update { it.copy(transactions = list) }
            }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            val user = authRepository.getAuthState().firstOrNull()
            if (user != null) {
                _uiState.update { it.copy(balance = user.balance) }
            }
        }
    }

    fun topUp(amount: Double, provider: WalletProvider) {
        if (amount <= 0) {
            _uiState.update { it.copy(errorMessage = "So tien nap khong hop le.", successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }
            
            val user = authRepository.getAuthState().firstOrNull()
            if (user == null) {
                _uiState.update { it.copy(isProcessing = false, errorMessage = "Vui long dang nhap lai.") }
                return@launch
            }

            // Gọi giả lập thanh toán nạp tiền
            val chargeResult = walletPaymentGateway.charge(
                provider = provider,
                amount = amount,
                orderId = "TOPUP-${System.currentTimeMillis()}",
                customerId = user.id
            )

            chargeResult.onSuccess {
                // Thanh toán thành công, cộng tiền vào tài khoản
                val updateResult = authRepository.updateBalance(amount)
                updateResult.onSuccess { updatedUser ->
                    // Ghi log giao dịch
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
                            successMessage = "Nap tien thanh cong tu ${provider.name}."
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Nap tien thanh cong nhung loi cap nhat so du: ${error.message}"
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Giao dich nap tien that bai: ${error.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
