package com.example.foodienow.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.data.repository.PaymentSettingsRepository
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.model.Voucher
import com.example.foodienow.domain.model.Address
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
import com.example.foodienow.domain.model.SystemSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import com.example.foodienow.data.remote.GoongAddressService
import com.example.foodienow.data.remote.GoongPrediction
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
    val errorMessage: String? = null,
    val selectedAddress: Address? = null,
    val checkoutUrl: String? = null,
    val pendingPaymentData: PendingPaymentData? = null,
    val addresses: List<Address> = emptyList(),
    val predictions: List<GoongPrediction> = emptyList(),
    val selectedLat: Double? = null,
    val selectedLng: Double? = null,
    val selectedDetail: String = "",
    val isResolving: Boolean = false,
    val baseDeliveryFee: Long = 15_000L,
    val freeDeliveryThreshold: Long = 100_000L,
    val userRole: com.example.foodienow.domain.model.UserRole? = null
)

data class PendingPaymentData(
    val method: PaymentMethod,
    val provider: WalletProvider?,
    val deliveryAddress: String,
    val note: String,
    val amount: Long,
    val usedRewardPoints: Int,
    val voucherCode: String?,
    val deliveryLat: Double?,
    val deliveryLng: Double?,
    val transactionId: String?
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
    private val paymentSettingsRepository: PaymentSettingsRepository,
    private val goongAddressService: GoongAddressService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _paymentEvent = MutableSharedFlow<PaymentEvent>()
    val paymentEvent: SharedFlow<PaymentEvent> = _paymentEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(rewardPointsAvailable = 0, userRole = null) }
                    return@collect
                }

                _uiState.update { it.copy(rewardPointsAvailable = user.rewardPoints, userRole = user.role) }

                runCatching {
                    profileRepository.getProfile(user.id).first()
                }.onSuccess { profile ->
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
        }
        viewModelScope.launch {
            addressRepository.addresses.collect { addresses ->
                val defaultAddrObj = addresses.firstOrNull { it.isDefault }
                val defaultAddrStr = defaultAddrObj?.detail ?: ""
                _uiState.update {
                    it.copy(
                        defaultAddress = defaultAddrStr,
                        selectedAddress = defaultAddrObj,
                        addresses = addresses
                    )
                }
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
        viewModelScope.launch {
            paymentRepository.getSystemSettings()
                .onSuccess { settings ->
                    val baseFee = settings.find { it.key == "base_delivery_fee" }?.value?.toLong() ?: 15_000L
                    val threshold = settings.find { it.key == "free_delivery_threshold" }?.value?.toLong() ?: 100_000L
                    _uiState.update {
                        it.copy(
                            baseDeliveryFee = baseFee,
                            freeDeliveryThreshold = threshold
                        )
                    }
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
        voucherCode: String? = null,
        deliveryLat: Double? = null,
        deliveryLng: Double? = null
    ) {
        if (deliveryAddress.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Dia chi nhan hang khong duoc de trong.", infoMessage = null)
            }
            return
        }
        val currentState = _uiState.value
        val finalLat = deliveryLat ?: currentState.selectedLat ?: currentState.selectedAddress?.latitude
        val finalLng = deliveryLng ?: currentState.selectedLng ?: currentState.selectedAddress?.longitude

        viewModelScope.launch {
            try {
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

                if (sessionUser.role == com.example.foodienow.domain.model.UserRole.ADMIN && method == PaymentMethod.COD) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Admin không được sử dụng phương thức thanh toán bằng tiền mặt (COD)."
                        )
                    }
                    return@launch
                }

                val refreshResult = runCatching { authRepository.refreshSession() }
                val user = refreshResult.getOrNull()?.getOrNull() ?: sessionUser

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
                        if (charge?.paymentUrl != null) {
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    checkoutUrl = charge.paymentUrl,
                                    pendingPaymentData = PendingPaymentData(
                                        method = method,
                                        provider = provider,
                                        deliveryAddress = deliveryAddress,
                                        note = note,
                                        amount = amount,
                                        usedRewardPoints = usedRewardPoints,
                                        voucherCode = voucherCode,
                                        deliveryLat = finalLat,
                                        deliveryLng = finalLng,

                                        transactionId = charge.transactionId
                                    )
                                )
                            }
                            return@launch
                        }

                        executeFinalPayment(
                            method = method,
                            provider = provider,
                            deliveryAddress = deliveryAddress,
                            note = note,
                            amount = amount,
                            usedRewardPoints = usedRewardPoints,
                            voucherCode = voucherCode,
                            deliveryLat = finalLat,
                            deliveryLng = finalLng,

                            transactionId = charge?.transactionId,
                            user = user,
                            cartItems = cartItems
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = e.message ?: "Da xay ra loi khi xu ly thanh toan."
                    )
                }
            }
        }
    }

    private fun executeFinalPayment(
        method: PaymentMethod,
        provider: WalletProvider?,
        deliveryAddress: String,
        note: String,
        amount: Long,
        usedRewardPoints: Int,
        voucherCode: String?,
        deliveryLat: Double?,
        deliveryLng: Double?,
        transactionId: String?,
        user: com.example.foodienow.domain.model.User,
        cartItems: Map<com.example.foodienow.domain.model.Food, Int>
    ) {
        viewModelScope.launch {
            try {
                paymentRepository.processPaymentAtomic(
                    AtomicPaymentRequest(
                        customerId = user.id,
                        amount = amount,
                        method = method,
                        provider = provider,
                        transactionId = transactionId,
                        deliveryAddress = deliveryAddress,
                        note = note.ifBlank { null },
                        usedRewardPoints = usedRewardPoints,
                        items = cartItems.map { (food, quantity) ->
                            PaymentLineItem(foodId = food.id, quantity = quantity)
                        },
                        voucherCode = voucherCode?.trim()?.takeIf { it.isNotBlank() },
                        accessToken = user.token,
                        deliveryLat = deliveryLat,
                        deliveryLng = deliveryLng
                    )
                )
                    .onSuccess { result ->
                        runCatching {
                            authRepository.updateSessionFinancials(
                                balance = result.newBalance,
                                rewardPoints = result.newRewardPoints
                            )
                        }
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                infoMessage = "Thanh toan thanh cong."
                            )
                        }
                        runCatching { cartRepository.clearCart() }
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
                            runCatching { authRepository.logout() }
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = e.message ?: "Da xay ra loi khi xu ly thanh toan."
                    )
                }
            }
        }
    }

    fun handleWebViewResult(success: Boolean) {
        val state = _uiState.value
        val pendingData = state.pendingPaymentData
        
        _uiState.update { it.copy(checkoutUrl = null, pendingPaymentData = null) }
        
        if (success && pendingData != null) {
            viewModelScope.launch {
                try {
                    val user = authRepository.getAuthState().first() ?: return@launch
                    val cartItems = cartRepository.cartItems.first()
                    _uiState.update { it.copy(isProcessing = true) }
                    executeFinalPayment(
                        method = pendingData.method,
                        provider = pendingData.provider,
                        deliveryAddress = pendingData.deliveryAddress,
                        note = pendingData.note,
                        amount = pendingData.amount,
                        usedRewardPoints = pendingData.usedRewardPoints,
                        voucherCode = pendingData.voucherCode,
                        deliveryLat = pendingData.deliveryLat,
                        deliveryLng = pendingData.deliveryLng,
                        transactionId = pendingData.transactionId,
                        user = user,
                        cartItems = cartItems
                    )
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = e.message ?: "Da xay ra loi khi xu ly thanh toan."
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(errorMessage = "Giao dich da bi huy hoac that bai.") }
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

    fun selectSavedAddress(address: Address) {
        _uiState.update {
            it.copy(
                selectedAddress = address,
                defaultAddress = address.detail,
                selectedLat = address.latitude,
                selectedLng = address.longitude
            )
        }
    }

    fun searchAddress(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { it.copy(predictions = emptyList()) }
            } else {
                val results = goongAddressService.getAutocomplete(query)
                _uiState.update { it.copy(predictions = results) }
            }
        }
    }

    fun selectPrediction(prediction: GoongPrediction) {
        _uiState.update {
            it.copy(
                predictions = emptyList(),
                selectedDetail = prediction.description,
                isResolving = true
            )
        }
        viewModelScope.launch {
            val result = goongAddressService.getPlaceDetail(prediction.placeId)
            if (result != null) {
                _uiState.update {
                    it.copy(
                        selectedLat = result.geometry.location.lat,
                        selectedLng = result.geometry.location.lng,
                        selectedDetail = result.formattedAddress ?: result.name ?: prediction.description,
                        isResolving = false
                    )
                }
            } else {
                _uiState.update { it.copy(isResolving = false) }
            }
        }
    }

    fun updateLocation(lat: Double, lng: Double, detail: String? = null) {
        _uiState.update { it.copy(selectedLat = lat, selectedLng = lng) }
        if (detail != null) {
            _uiState.update { it.copy(selectedDetail = detail) }
        } else {
            viewModelScope.launch {
                val result = goongAddressService.getReverseGeocode(lat, lng)
                if (result != null) {
                    _uiState.update {
                        it.copy(selectedDetail = result.formattedAddress ?: result.name ?: "")
                    }
                }
            }
        }
    }

    fun clearAddressForm() {
        _uiState.update {
            it.copy(
                predictions = emptyList(),
                selectedLat = null,
                selectedLng = null,
                selectedDetail = "",
                isResolving = false
            )
        }
    }

    private fun PaymentMethod.toDisplayLabel(provider: WalletProvider?): String {
        return when (this) {
            PaymentMethod.COD -> "Tien mat (COD)"
            PaymentMethod.WALLET -> provider?.name ?: "Vi dien tu"
            PaymentMethod.FOODIE_PAY -> "FoodiePay"
        }
    }
}
