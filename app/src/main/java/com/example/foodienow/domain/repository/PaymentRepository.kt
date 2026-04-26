package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun createPayment(payment: Payment): Result<Payment>

    fun getPaymentsByCustomer(customerId: String): Flow<List<Payment>>
}

