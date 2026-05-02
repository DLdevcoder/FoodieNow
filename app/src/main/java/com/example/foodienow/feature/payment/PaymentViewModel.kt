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
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

sealed class PaymentEvent {
    object PaymentSuccess : PaymentEvent()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val notificationRepository: NotificationRepository,
    private val walletPaymentGateway: WalletPaymentGateway,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _paymentEvent = MutableSharedFlow<PaymentEvent>()
    val paymentEvent: SharedFlow<PaymentEvent> = _paymentEvent.asSharedFlow()

    fun submitPayment(
        method: PaymentMethod,
        provider: WalletProvider?,
        deliveryAddress: String,
        note: String,
        amount: Double
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
                                    createNotification(
                                        userId = user.id,
                                        title = "Thanh toan thanh cong",
                                        message = "Don hang ${createdOrder.id ?: ""} da thanh toan thanh cong."
                                    )
                                    _uiState.update {
                                        it.copy(
                                            isProcessing = false,
                                            infoMessage = "Thanh toan thanh cong. Don hang da duoc luu vao he thong."
                                        )
                                    }
                                    cartRepository.clearCart()
                                    _paymentEvent.emit(PaymentEvent.PaymentSuccess)
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
            append(note.ifBlank { "Thanh toan that bai" })
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

        createdOrder.id?.let { orderId ->
            orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
        }

        createNotification(
            userId = customerId,
            title = "Thanh toan that bai",
            message = "Don hang ${createdOrder.id ?: ""} da bi huy do giao dich loi."
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

