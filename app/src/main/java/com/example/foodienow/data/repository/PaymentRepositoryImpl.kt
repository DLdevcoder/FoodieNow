package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.repository.AtomicPaymentRequest
import com.example.foodienow.domain.repository.AtomicPaymentResult
import com.example.foodienow.domain.repository.PaymentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

    override suspend fun processPaymentAtomic(request: AtomicPaymentRequest): Result<AtomicPaymentResult> {
        return try {
            val response = supabaseClient.postgrest.rpc(
                function = "process_payment",
                parameters = ProcessPaymentRpcRequest(
                    customerId = request.customerId,
                    amount = request.amount,
                    method = request.method.name,
                    provider = request.provider?.name,
                    transactionId = request.transactionId,
                    deliveryAddress = request.deliveryAddress,
                    note = request.note,
                    usedRewardPoints = request.usedRewardPoints
                )
            ).decodeSingle<ProcessPaymentRpcResponse>()

            Result.success(
                AtomicPaymentResult(
                    orderId = response.orderId,
                    paymentId = response.paymentId,
                    earnedPoints = response.earnedPoints
                )
            )
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

@Serializable
private data class ProcessPaymentRpcRequest(
    @SerialName("p_customer_id") val customerId: String,
    @SerialName("p_amount") val amount: Long,
    @SerialName("p_method") val method: String,
    @SerialName("p_provider") val provider: String?,
    @SerialName("p_transaction_id") val transactionId: String?,
    @SerialName("p_delivery_address") val deliveryAddress: String,
    @SerialName("p_note") val note: String?,
    @SerialName("p_used_reward_points") val usedRewardPoints: Int
)

@Serializable
private data class ProcessPaymentRpcResponse(
    @SerialName("order_id") val orderId: String,
    @SerialName("payment_id") val paymentId: String,
    @SerialName("earned_points") val earnedPoints: Int
)
