package com.example.foodienow.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.PaymentStatus
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.model.AppNotification
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.payment.WalletChargeResult
import com.example.foodienow.domain.payment.WalletPaymentGateway
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.NotificationRepository
import com.example.foodienow.domain.repository.OrderRepository
import com.example.foodienow.domain.repository.PaymentRepository
import com.example.foodienow.domain.repository.CartRepository
import com.example.foodienow.data.repository.MockWalletTransactionRepository
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.data.repository.MockPaymentSettingsRepository
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.model.WalletTransactionType
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
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

sealed class PaymentEvent {
    data class PaymentSuccess(
        val orderId: String,
        val amount: Double,
        val methodLabel: String
    ) : PaymentEvent()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val notificationRepository: NotificationRepository,
    private val walletPaymentGateway: WalletPaymentGateway,
    private val cartRepository: CartRepository,
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

    fun applyVoucher(code: String): Double {
        return when (code.uppercase()) {
            "GIAM20K" -> 20000.0
            "FREESHIP" -> 15000.0 // Giả sử freeship là 15k
            else -> 0.0
        }
    }

    fun submitPayment(
        method: PaymentMethod,
        provider: WalletProvider?,
        deliveryAddress: String,
        note: String,
        amount: Double,
        usedRewardPoints: Int = 0
    ) {
        if (deliveryAddress.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Địa chỉ nhận hàng không được để trống.", infoMessage = null)
            }
            return
        }

        if (method == PaymentMethod.CARD) {
            _uiState.update {
                it.copy(errorMessage = "Thanh toán bằng thẻ đang được phát triển. Vui lòng chọn phương thức khác.", infoMessage = null)
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
                        errorMessage = "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại."
                    )
                }
                return@launch
            }

            if (method == PaymentMethod.WALLET && provider == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Vui lòng chọn ví điện tử để thanh toán."
                    )
                }
                return@launch
            }

            if (method == PaymentMethod.FOODIE_PAY) {
                if (user.balance < amount) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "So du Ví FoodiePay khong du. Vui long nap them tien."
                        )
                    }
                    return@launch
                }
            }

            val orderResult = orderRepository.createOrder(
                Order(
                    customerId = user.id,
                    totalPrice = amount,
                    deliveryAddress = deliveryAddress,
                    note = note.ifBlank { null }
                )
            )

            orderResult
                .onSuccess { createdOrder ->
                    val walletResult: Result<WalletChargeResult?> = if (method == PaymentMethod.WALLET && provider != null) {
                        walletPaymentGateway.charge(
                            provider = provider,
                            amount = amount,
                            orderId = createdOrder.id ?: "",
                            customerId = user.id
                        ).map { it }
                    } else if (method == PaymentMethod.FOODIE_PAY) {
                        // Trừ tiền trong ví
                        val deductResult = authRepository.updateBalance(-amount)
                        if (deductResult.isSuccess) {
                            // Ghi log giao dịch ví
                            walletTransactionRepository.addTransaction(
                                WalletTransaction(
                                    id = "FPAY-${System.currentTimeMillis()}",
                                    type = WalletTransactionType.PAYMENT,
                                    amount = amount,
                                    description = "Thanh toán đơn hàng",
                                    createdAt = java.time.Instant.now().toString()
                                )
                            )
                            Result.success(WalletChargeResult(transactionId = "FPAY-${System.currentTimeMillis()}", message = "FoodiePay success"))
                        } else {
                            Result.failure(Exception("Loi tru tien FoodiePay"))
                        }
                    } else {
                        Result.success(null)
                    }

                    walletResult
                        .onSuccess { charge ->
                            paymentRepository.createPayment(
                                Payment(
                                    customerId = user.id,
                                    orderId = createdOrder.id,
                                    amount = amount,
                                    method = method,
                                    provider = provider,
                                    transactionId = charge?.transactionId,
                                    status = PaymentStatus.SUCCESS,
                                    deliveryAddress = deliveryAddress,
                                    note = note.ifBlank { null }
                                )
                            )
                                .onSuccess {
                                    val earnedPoints = (amount * 0.01).toInt()
                                    val pointDiff = earnedPoints - usedRewardPoints
                                    if (pointDiff != 0) {
                                        authRepository.updateRewardPoints(pointDiff)
                                    }

                                    createNotification(
                                        userId = user.id,
                                        title = "Thanh toán thành công",
                                        message = "Đơn hàng ${createdOrder.id ?: ""} đã thanh toán. Bạn được cộng $earnedPoints FoodieCoins."
                                    )
                                    val methodLabel = when (method) {
                                        PaymentMethod.COD -> "Tiền mặt (COD)"
                                        PaymentMethod.CARD -> "Thẻ tín dụng"
                                        PaymentMethod.WALLET -> provider?.name ?: "Ví điện tử"
                                        PaymentMethod.FOODIE_PAY -> "FoodiePay"
                                    }
                                    _uiState.update {
                                        it.copy(
                                            isProcessing = false,
                                            infoMessage = "Thanh toán thành công."
                                        )
                                    }
                                    cartRepository.clearCart()
                                    _paymentEvent.emit(PaymentEvent.PaymentSuccess(
                                        orderId = createdOrder.id ?: "",
                                        amount = amount,
                                        methodLabel = methodLabel
                                    ))
                                }
                                .onFailure { error ->
                                    handlePaymentFailure(
                                        customerId = user.id,
                                        createdOrder = createdOrder,
                                        method = method,
                                        provider = provider,
                                        transactionId = charge?.transactionId,
                                        deliveryAddress = deliveryAddress,
                                        note = note,
                                        amount = amount,
                                        cause = error
                                    )
                                    _uiState.update {
                                        it.copy(
                                            isProcessing = false,
                                            errorMessage = "Thanh toan that bai. Don hang da duoc huy de tranh sai lech du lieu."
                                        )
                                    }
                                }
                        }
                        .onFailure { error ->
                            handlePaymentFailure(
                                customerId = user.id,
                                createdOrder = createdOrder,
                                method = method,
                                provider = provider,
                                transactionId = null,
                                deliveryAddress = deliveryAddress,
                                note = note,
                                amount = amount,
                                cause = error
                            )
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    errorMessage = "Giao dich vi dien tu that bai. Don hang da duoc huy."
                                )
                            }
                        }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = error.message ?: "Khong tao duoc don hang."
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }

    private suspend fun handlePaymentFailure(
        customerId: String,
        createdOrder: Order,
        method: PaymentMethod,
        provider: WalletProvider?,
        transactionId: String?,
        deliveryAddress: String,
        note: String,
        amount: Double,
        cause: Throwable
    ) {
        val failedNote = buildString {
            append(note.ifBlank { "Thanh toán thất bại" })
            append(" | error=")
            append(cause.message ?: "unknown")
        }

        paymentRepository.createPayment(
            Payment(
                customerId = customerId,
                orderId = createdOrder.id,
                amount = amount,
                method = method,
                provider = provider,
                transactionId = transactionId,
                status = PaymentStatus.FAILED,
                deliveryAddress = deliveryAddress,
                note = failedNote
            )
        )

        if (method == PaymentMethod.FOODIE_PAY) {
            authRepository.updateBalance(amount)
            walletTransactionRepository.addTransaction(
                WalletTransaction(
                    id = "REFUND-${System.currentTimeMillis()}",
                    type = WalletTransactionType.TOP_UP,
                    amount = amount,
                    description = "Hoàn tiền đơn hàng ${createdOrder.id ?: ""}",
                    createdAt = java.time.Instant.now().toString()
                )
            )
        }

        createdOrder.id?.let { orderId ->
            orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
        }

        createNotification(
            userId = customerId,
            title = "Thanh toán thất bại",
            message = "Đơn hàng ${createdOrder.id ?: ""} đã bị hủy do giao dịch lỗi.${if (method == PaymentMethod.FOODIE_PAY) " Số tiền đã được hoàn lại vào ví FoodiePay." else ""}"
        )
    }

    private suspend fun createNotification(userId: String, title: String, message: String) {
        runCatching {
            notificationRepository.createNotification(
                AppNotification(
                    userId = userId,
                    title = title,
                    message = message
                )
            )
        }
    }
}

