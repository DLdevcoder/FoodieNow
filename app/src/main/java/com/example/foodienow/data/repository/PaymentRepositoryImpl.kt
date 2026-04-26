package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.repository.PaymentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : PaymentRepository {

    override suspend fun createPayment(payment: Payment): Result<Payment> {
        return try {
            val createdPayment = supabaseClient.postgrest["payments"]
                .insert(payment) {
                    select()
                }
                .decodeSingle<Payment>()
            Result.success(createdPayment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPaymentsByCustomer(customerId: String): Flow<List<Payment>> = flow {
        val payments = supabaseClient.postgrest["payments"]
            .select {
                filter {
                    eq("customer_id", customerId)
                }
            }
            .decodeList<Payment>()
        emit(payments)
    }
}

