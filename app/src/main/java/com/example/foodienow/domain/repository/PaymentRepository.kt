package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import kotlinx.coroutines.flow.Flow

data class PaymentLineItem(
    val foodId: String,
    val quantity: Int
)

data class AtomicPaymentRequest(
    val customerId: String,
    val amount: Long,
    val method: PaymentMethod,
    val provider: WalletProvider?,
    val transactionId: String?,
    val deliveryAddress: String,
    val note: String?,
    val usedRewardPoints: Int,
    val items: List<PaymentLineItem>,
    val voucherCode: String?,
    val accessToken: String,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null
)

data class AtomicPaymentResult(
    val orderId: String,
    val paymentId: String,
    val amountCharged: Long,
    val deliveryFee: Long,
    val discountAmount: Long,
    val earnedPoints: Int,
    val newRewardPoints: Int,
    val newBalance: Long
)

interface PaymentRepository {
    suspend fun createPayment(payment: Payment): Result<Payment>

    suspend fun processPaymentAtomic(request: AtomicPaymentRequest): Result<AtomicPaymentResult>

    fun getPaymentsByCustomer(customerId: String): Flow<List<Payment>>
}

