package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import kotlinx.coroutines.flow.Flow

data class AtomicPaymentRequest(
    val customerId: String,
    val amount: Long,
    val method: PaymentMethod,
    val provider: WalletProvider?,
    val transactionId: String?,
    val deliveryAddress: String,
    val note: String?,
    val usedRewardPoints: Int
)

data class AtomicPaymentResult(
    val orderId: String,
    val paymentId: String,
    val earnedPoints: Int
)

interface PaymentRepository {
    suspend fun createPayment(payment: Payment): Result<Payment>

    suspend fun processPaymentAtomic(request: AtomicPaymentRequest): Result<AtomicPaymentResult>

    fun getPaymentsByCustomer(customerId: String): Flow<List<Payment>>
}

