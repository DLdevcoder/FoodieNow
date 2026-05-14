package com.example.foodienow.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.data.repository.MockPaymentSettingsRepository
import com.example.foodienow.data.repository.MockWalletTransactionRepository
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.model.WalletTransactionType
import com.example.foodienow.domain.payment.WalletChargeResult
import com.example.foodienow.domain.payment.WalletPaymentGateway
import com.example.foodienow.domain.repository.AtomicPaymentRequest
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.CartRepository
import com.example.foodienow.domain.repository.PaymentRepository
import com.example.foodienow.domain.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class PaymentUiState(
    val isProcessing: Boolean = false,
    val rewardPointsAvailable: Int = 0,
    val defaultAddress: String = "",
    val defaultPaymentMethod: PaymentMethod = PaymentMethod.COD,
    val defaultWalletProvider: WalletProvider = WalletProvider.ZALOPAY,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

sealed class PaymentEvent {
    data class PaymentSuccess(
        val orderId: String,
        val amount: Long,
        val methodLabel: String
    ) : PaymentEvent()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentRepository,
    private val walletPaymentGateway: WalletPaymentGateway,
    private val cartRepository: CartRepository,
    private val voucherRepository: VoucherRepository,
    private val walletTransactionRepository: MockWalletTransactionRepository,
    private val addressRepository: MockAddressRepository,
    private val paymentSettingsRepository: MockPaymentSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _paymentEvent = MutableSharedFlow<PaymentEvent>()
    val paymentEvent: SharedFlow<PaymentEvent> = _paymentEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                _uiState.update { it.copy(rewardPointsAvailable = user?.rewardPoints ?: 0) }
            }
        }
        viewModelScope.launch {
            addressRepository.addresses.collect { addresses ->
                val defaultAddr = addresses.firstOrNull { it.isDefault }?.detail ?: ""
                _uiState.update { it.copy(defaultAddress = defaultAddr) }
            }
        }
        viewModelScope.launch {
            paymentSettingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        defaultPaymentMethod = settings.defaultMethod,
                        defaultWalletProvider = settings.defaultProvider
                    )
                }
            }
        }
    }

    suspend fun applyVoucher(code: String): Long {
        return voucherRepository.getDiscount(code)
    }

    fun submitPayment(
        method: PaymentMethod,
        provider: WalletProvider?,
        deliveryAddress: String,
        note: String,
        amount: Long,
        usedRewardPoints: Int = 0
    ) {
        if (deliveryAddress.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Dia chi nhan hang khong duoc de trong.", infoMessage = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, infoMessage = null, errorMessage = null) }
            val user = authRepository.getAuthState().first()
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Phien dang nhap khong hop le. Vui long dang nhap lai."
                    )
                }
                return@launch
            }

            if (method == PaymentMethod.WALLET && provider == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Vui long chon vi dien tu de thanh toan."
                    )
                }
                return@launch
            }

            if (method == PaymentMethod.FOODIE_PAY && user.balance < amount) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "So du FoodiePay khong du. Vui long nap them tien."
                    )
                }
                return@launch
            }

            val chargeResult = prepareClientSideCharge(
                method = method,
                provider = provider,
                amount = amount,
                customerId = user.id
            )

            chargeResult
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = error.message ?: "Giao dich vi dien tu that bai."
                        )
                    }
                }
                .onSuccess { charge ->
                    paymentRepository.processPaymentAtomic(
                        AtomicPaymentRequest(
                            customerId = user.id,
                            amount = amount,
                            method = method,
                            provider = provider,
                            transactionId = charge?.transactionId,
                            deliveryAddress = deliveryAddress,
                            note = note.ifBlank { null },
                            usedRewardPoints = usedRewardPoints
                        )
                    )
                        .onSuccess { result ->
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    infoMessage = "Thanh toan thanh cong."
                                )
                            }
                            cartRepository.clearCart()
                            _paymentEvent.emit(
                                PaymentEvent.PaymentSuccess(
                                    orderId = result.orderId,
                                    amount = amount,
                                    methodLabel = method.toDisplayLabel(provider)
                                )
                            )
                        }
                        .onFailure { error ->
                            refundFoodiePayIfNeeded(method, amount)
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    errorMessage = error.message
                                        ?: "Thanh toan that bai. Du lieu don hang da duoc rollback."
                                )
                            }
                        }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }

    private suspend fun prepareClientSideCharge(
        method: PaymentMethod,
        provider: WalletProvider?,
        amount: Long,
        customerId: String
    ): Result<WalletChargeResult?> {
        return when {
            method == PaymentMethod.WALLET && provider != null -> {
                walletPaymentGateway.charge(
                    provider = provider,
                    amount = amount,
                    orderId = "PENDING-${System.currentTimeMillis()}",
                    customerId = customerId
                ).map { it }
            }
            method == PaymentMethod.FOODIE_PAY -> {
                val transactionId = "FPAY-${System.currentTimeMillis()}"
                authRepository.updateBalance(-amount).map {
                    walletTransactionRepository.addTransaction(
                        WalletTransaction(
                            id = transactionId,
                            type = WalletTransactionType.PAYMENT,
                            amount = amount,
                            description = "Thanh toan don hang",
                            createdAt = Instant.now().toString()
                        )
                    )
                    WalletChargeResult(transactionId = transactionId, message = "FoodiePay success")
                }
            }
            else -> Result.success(null)
        }
    }

    private suspend fun refundFoodiePayIfNeeded(method: PaymentMethod, amount: Long) {
        if (method != PaymentMethod.FOODIE_PAY) return

        authRepository.updateBalance(amount)
        walletTransactionRepository.addTransaction(
            WalletTransaction(
                id = "REFUND-${System.currentTimeMillis()}",
                type = WalletTransactionType.TOP_UP,
                amount = amount,
                description = "Hoan tien don hang thanh toan loi",
                createdAt = Instant.now().toString()
            )
        )
    }

    private fun PaymentMethod.toDisplayLabel(provider: WalletProvider?): String {
        return when (this) {
            PaymentMethod.COD -> "Tien mat (COD)"
            PaymentMethod.CARD -> "The tin dung"
            PaymentMethod.WALLET -> provider?.name ?: "Vi dien tu"
            PaymentMethod.FOODIE_PAY -> "FoodiePay"
        }
    }
}
