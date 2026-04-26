package com.example.foodienow.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.PaymentStatus
import com.example.foodienow.domain.model.AppNotification
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.NotificationRepository
import com.example.foodienow.domain.repository.OrderRepository
import com.example.foodienow.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun submitPayment(
        method: PaymentMethod,
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
                    paymentRepository.createPayment(
                        Payment(
                            customerId = user.id,
                            orderId = createdOrder.id,
                            amount = amount,
                            method = method,
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
                        }
                        .onFailure { error ->
                            handlePaymentFailure(
                                customerId = user.id,
                                createdOrder = createdOrder,
                                method = method,
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

