package com.example.foodienow.data.repository

import com.example.foodienow.data.remote.SupabaseRest
import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.repository.AtomicPaymentRequest
import com.example.foodienow.domain.repository.AtomicPaymentResult
import com.example.foodienow.domain.repository.PaymentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
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

    override suspend fun processPaymentAtomic(request: AtomicPaymentRequest): Result<AtomicPaymentResult> =
        withContext(Dispatchers.IO) {
            try {
                val response = SupabaseRest.post(
                    path = "/rest/v1/rpc/process_payment",
                    body = PaymentRpcMapper.toRpcBody(request),
                    accessToken = request.accessToken
                )

                if (!response.isSuccess) {
                    return@withContext Result.failure(
                        Exception(SupabaseRest.parseErrorMessage(response.body))
                    )
                }

                Result.success(PaymentRpcMapper.toAtomicPaymentResult(response.body))
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

internal object PaymentRpcMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun toPayload(request: AtomicPaymentRequest): PaymentRpcPayload {
        return PaymentRpcPayload(
            customerId = request.customerId,
            amount = request.amount,
            method = request.method.name,
            provider = request.provider?.name,
            transactionId = request.transactionId,
            deliveryAddress = request.deliveryAddress,
            note = request.note,
            usedRewardPoints = request.usedRewardPoints,
            items = request.items.map { item ->
                PaymentRpcItem(foodId = item.foodId, quantity = item.quantity)
            },
            voucherCode = request.voucherCode,
            deliveryLat = request.deliveryLat,
            deliveryLng = request.deliveryLng
        )
    }

    fun toRpcBody(request: AtomicPaymentRequest): JSONObject {
        val payload = toPayload(request)
        val itemPayload = JSONArray().apply {
            payload.items.forEach { item ->
                put(
                    JSONObject()
                        .put("food_id", item.foodId)
                        .put("quantity", item.quantity)
                )
            }
        }

        return JSONObject()
            .put("p_customer_id", payload.customerId)
            .put("p_amount", payload.amount)
            .put("p_method", payload.method)
            .put("p_provider", payload.provider ?: JSONObject.NULL)
            .put("p_transaction_id", payload.transactionId ?: JSONObject.NULL)
            .put("p_delivery_address", payload.deliveryAddress)
            .put("p_note", payload.note ?: JSONObject.NULL)
            .put("p_used_reward_points", payload.usedRewardPoints)
            .put("p_items", itemPayload)
            .put("p_voucher_code", payload.voucherCode ?: JSONObject.NULL)
            .put("p_delivery_lat", payload.deliveryLat ?: JSONObject.NULL)
            .put("p_delivery_lng", payload.deliveryLng ?: JSONObject.NULL)
    }

    fun toAtomicPaymentResult(body: String): AtomicPaymentResult {
        val payload = when (val element = json.parseToJsonElement(body)) {
            is JsonArray -> {
                if (element.isEmpty()) {
                    throw IllegalStateException("Payment RPC returned no result.")
                }
                element.first().jsonObject
            }
            else -> element.jsonObject
        }

        return AtomicPaymentResult(
            orderId = payload.getString("order_id"),
            paymentId = payload.getString("payment_id"),
            amountCharged = payload.getLongNumber("amount_charged"),
            deliveryFee = payload.getLongNumber("delivery_fee"),
            discountAmount = payload.getLongNumber("discount_amount"),
            earnedPoints = payload.getIntNumber("earned_points"),
            newRewardPoints = payload.getIntNumber("new_reward_points"),
            newBalance = payload.getLongNumber("new_balance")
        )
    }

    private fun JsonObject.getString(name: String): String {
        return getValue(name).jsonPrimitive.content
    }

    private fun JsonObject.getIntNumber(name: String): Int {
        return getValue(name).jsonPrimitive.content.toInt()
    }

    private fun JsonObject.getLongNumber(name: String): Long {
        val value = getValue(name).jsonPrimitive
        return value.longOrNull ?: BigDecimal(value.content).toLong()
    }
}

internal data class PaymentRpcPayload(
    val customerId: String,
    val amount: Long,
    val method: String,
    val provider: String?,
    val transactionId: String?,
    val deliveryAddress: String,
    val note: String?,
    val usedRewardPoints: Int,
    val items: List<PaymentRpcItem>,
    val voucherCode: String?,
    val deliveryLat: Double?,
    val deliveryLng: Double?
)

internal data class PaymentRpcItem(
    val foodId: String,
    val quantity: Int
)
