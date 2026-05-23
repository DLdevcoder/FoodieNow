package com.example.foodienow.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.data.repository.PaymentSettingsRepository
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.model.Voucher
import com.example.foodienow.domain.payment.PaymentMethodCatalog
import com.example.foodienow.domain.payment.WalletChargeResult
import com.example.foodienow.domain.payment.WalletPaymentGateway
import com.example.foodienow.domain.repository.AtomicPaymentRequest
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.CartRepository
import com.example.foodienow.domain.repository.PaymentLineItem
import com.example.foodienow.domain.repository.PaymentRepository
import com.example.foodienow.domain.repository.ProfileRepository
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
import javax.inject.Inject

data class PaymentUiState(
    val isProcessing: Boolean = false,
    val rewardPointsAvailable: Int = 0,
    val defaultAddress: String = "",
    val defaultPaymentMethod: PaymentMethod = PaymentMethod.COD,
    val defaultWalletProvider: WalletProvider = WalletProvider.ZALOPAY,
    val configuredPaymentOptionIds: Set<String> = PaymentMethodCatalog.alwaysAvailableOptionIds,
    val paymentSettingsLoaded: Boolean = false,
    val availableVouchers: List<Voucher> = emptyList(),
    val selectedVoucher: Voucher? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

sealed class PaymentEvent {
    data class PaymentSuccess(
        val orderId: String,
        val amount: Long,
        val methodLabel: String
    ) : PaymentEvent()
    object SessionExpired : PaymentEvent()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentRepository,
    private val walletPaymentGateway: WalletPaymentGateway,
    private val cartRepository: CartRepository,
    private val voucherRepository: VoucherRepository,
    private val profileRepository: ProfileRepository,
    private val addressRepository: MockAddressRepository,
    private val paymentSettingsRepository: PaymentSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _paymentEvent = MutableSharedFlow<PaymentEvent>()
    val paymentEvent: SharedFlow<PaymentEvent> = _paymentEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(rewardPointsAvailable = 0) }
                    return@collect
                }

                _uiState.update { it.copy(rewardPointsAvailable = user.rewardPoints) }

                val profile = profileRepository.getProfile(user.id).first()
                if (profile != null &&
                    (profile.balance != user.balance || profile.rewardPoints != user.rewardPoints)
                ) {
                    authRepository.updateSessionFinancials(
                        balance = profile.balance,
                        rewardPoints = profile.rewardPoints
                    )
                    _uiState.update { it.copy(rewardPointsAvailable = profile.rewardPoints) }
                }
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
                        defaultWalletProvider = settings.defaultProvider,
                        configuredPaymentOptionIds = PaymentMethodCatalog.alwaysAvailableOptionIds +
                            settings.configuredOptionIds,
                        paymentSettingsLoaded = settings.isLoaded
                    )
                }
            }
        }
        viewModelScope.launch {
            paymentSettingsRepository.refreshSettings()
                .onFailure {
                    _uiState.update { state -> state.copy(paymentSettingsLoaded = true) }
                }
        }
    }

    suspend fun applyVoucher(code: String, storeId: String?, subtotal: Long): Long {
        if (storeId.isNullOrBlank() || code.isBlank()) return 0L

        return voucherRepository.quoteDiscount(code, storeId, subtotal)
            .fold(
                onSuccess = { it.discountAmount },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message ?: "Khong ap dung duoc ma giam gia.",
                            infoMessage = null
                        )
                    }
                    0L
                }
            )
    }

    fun submitPayment(
        method: PaymentMethod,
        provider: WalletProvider?,
        deliveryAddress: String,
        note: String,
        amount: Long,
        usedRewardPoints: Int = 0,
        voucherCode: String? = null
    ) {
        if (deliveryAddress.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Dia chi nhan hang khong duoc de trong.", infoMessage = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, infoMessage = null, errorMessage = null) }

            val sessionUser = authRepository.getAuthState().first()
            if (sessionUser == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Phien dang nhap khong hop le. Vui long dang nhap lai."
                    )
                }
                return@launch
            }

            val refreshResult = authRepository.refreshSession()
            val user = refreshResult.getOrNull() ?: sessionUser

            val cartItems = cartRepository.cartItems.first()
            if (cartItems.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Gio hang dang trong."
                    )
                }
                return@launch
            }

            if (cartItems.keys.map { it.storeId }.distinct().size != 1) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Chi co the thanh toan mon trong cung mot cua hang."
                    )
                }
                return@launch
            }

            if (method == PaymentMethod.WALLET && provider == null && amount > 0L) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Vui long chon vi dien tu de thanh toan."
                    )
                }
                return@launch
            }

            val paymentOptionId = PaymentMethodCatalog.optionIdFor(method, provider)
            val configuredOptionIds = paymentSettingsRepository.settings.value.configuredOptionIds
            if (!PaymentMethodCatalog.isOptionAvailable(paymentOptionId, configuredOptionIds)) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Vui long cai dat thong tin phuong thuc thanh toan truoc."
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
                            usedRewardPoints = usedRewardPoints,
                            items = cartItems.map { (food, quantity) ->
                                PaymentLineItem(foodId = food.id, quantity = quantity)
                            },
                            voucherCode = voucherCode?.trim()?.takeIf { it.isNotBlank() },
                            accessToken = user.token
                        )
                    )
                        .onSuccess { result ->
                            authRepository.updateSessionFinancials(
                                balance = result.newBalance,
                                rewardPoints = result.newRewardPoints
                            )
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
                                    amount = result.amountCharged,
                                    methodLabel = method.toDisplayLabel(provider)
                                )
                            )
                        }
                        .onFailure { error ->
                            val isJwtError = error.message?.contains("JWT", ignoreCase = true) == true
                            if (isJwtError) {
                                authRepository.logout()
                                _paymentEvent.emit(PaymentEvent.SessionExpired)
                            }
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    errorMessage = if (isJwtError) {
                                        "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                                    } else {
                                        error.message ?: "Thanh toan that bai. Du lieu don hang da duoc rollback."
                                    }
                                )
                            }
                        }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }

    fun calculateVoucherDiscount(voucher: Voucher, subtotal: Long): Long {
        if (subtotal < voucher.minOrderValue) return 0L
        val rawDiscount = if (voucher.discountAmount > 0L) {
            voucher.discountAmount
        } else {
            kotlin.math.floor(subtotal * voucher.discountPercent / 100.0).toLong()
        }
        val cappedDiscount = if (voucher.maxDiscount > 0L) {
            kotlin.math.min(rawDiscount, voucher.maxDiscount)
        } else {
            rawDiscount
        }
        return cappedDiscount.coerceIn(0L, subtotal)
    }

    fun loadAvailableVouchers(storeId: String, subtotal: Long) {
        if (storeId.isBlank()) return
        viewModelScope.launch {
            voucherRepository.getVouchersByStore(storeId)
                .onSuccess { vouchers ->
                    _uiState.update { it.copy(availableVouchers = vouchers) }
                    var bestVoucher: Voucher? = null
                    var maxDiscount = 0L
                    for (voucher in vouchers) {
                        if (subtotal >= voucher.minOrderValue) {
                            val discount = calculateVoucherDiscount(voucher, subtotal)
                            if (discount > maxDiscount) {
                                maxDiscount = discount
                                bestVoucher = voucher
                            }
                        }
                    }
                    _uiState.update { it.copy(selectedVoucher = bestVoucher) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun selectVoucher(voucher: Voucher?) {
        _uiState.update { it.copy(selectedVoucher = voucher) }
    }

    private suspend fun prepareClientSideCharge(
        method: PaymentMethod,
        provider: WalletProvider?,
        amount: Long,
        customerId: String
    ): Result<WalletChargeResult?> {
        return when {
            method == PaymentMethod.WALLET && provider != null && amount > 0L -> {
                walletPaymentGateway.charge(
                    provider = provider,
                    amount = amount,
                    orderId = "PENDING-${System.currentTimeMillis()}",
                    customerId = customerId
                ).map { it }
            }
            else -> Result.success(null)
        }
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
