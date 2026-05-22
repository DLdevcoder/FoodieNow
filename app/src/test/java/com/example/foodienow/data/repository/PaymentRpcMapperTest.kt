package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.repository.AtomicPaymentRequest
import com.example.foodienow.domain.repository.PaymentLineItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentRpcMapperTest {

    @Test
    fun toPayload_mapsCartItemsVoucherAndProvider() {
        val payload = PaymentRpcMapper.toPayload(
            AtomicPaymentRequest(
                customerId = "customer-id",
                amount = 75_000L,
                method = PaymentMethod.WALLET,
                provider = WalletProvider.MOMO,
                transactionId = "MOMO-SB-123",
                deliveryAddress = "123 Test Street",
                note = "No onion",
                usedRewardPoints = 1_000,
                items = listOf(
                    PaymentLineItem(foodId = "food-1", quantity = 2),
                    PaymentLineItem(foodId = "food-2", quantity = 1)
                ),
                voucherCode = "GIAMGIA20K",
                accessToken = "user-token"
            )
        )

        assertEquals("customer-id", payload.customerId)
        assertEquals(75_000L, payload.amount)
        assertEquals("WALLET", payload.method)
        assertEquals("MOMO", payload.provider)
        assertEquals("MOMO-SB-123", payload.transactionId)
        assertEquals("GIAMGIA20K", payload.voucherCode)

        assertEquals(2, payload.items.size)
        assertEquals("food-1", payload.items[0].foodId)
        assertEquals(2, payload.items[0].quantity)
    }

    @Test
    fun toPayload_keepsOptionalValuesNull() {
        val payload = PaymentRpcMapper.toPayload(
            AtomicPaymentRequest(
                customerId = "customer-id",
                amount = 0L,
                method = PaymentMethod.COD,
                provider = null,
                transactionId = null,
                deliveryAddress = "123 Test Street",
                note = null,
                usedRewardPoints = 0,
                items = listOf(PaymentLineItem(foodId = "food-1", quantity = 1)),
                voucherCode = null,
                accessToken = "user-token"
            )
        )

        assertTrue(payload.provider == null)
        assertTrue(payload.transactionId == null)
        assertTrue(payload.note == null)
        assertTrue(payload.voucherCode == null)
    }

    @Test
    fun toAtomicPaymentResult_parsesPostgrestTableResponse() {
        val result = PaymentRpcMapper.toAtomicPaymentResult(
            """
            [{
              "order_id": "order-id",
              "payment_id": "payment-id",
              "amount_charged": 55000,
              "delivery_fee": 15000,
              "discount_amount": 20000,
              "earned_points": 550,
              "new_reward_points": 1550,
              "new_balance": 945000
            }]
            """.trimIndent()
        )

        assertEquals("order-id", result.orderId)
        assertEquals("payment-id", result.paymentId)
        assertEquals(55_000L, result.amountCharged)
        assertEquals(15_000L, result.deliveryFee)
        assertEquals(20_000L, result.discountAmount)
        assertEquals(550, result.earnedPoints)
        assertEquals(1_550, result.newRewardPoints)
        assertEquals(945_000L, result.newBalance)
    }

    @Test
    fun toRpcBody_mapsJsonCorrectly() {
        val json = PaymentRpcMapper.toRpcBody(
            AtomicPaymentRequest(
                customerId = "customer-123",
                amount = 90_000L,
                method = PaymentMethod.WALLET,
                provider = WalletProvider.ZALOPAY,
                transactionId = "ZP-123",
                deliveryAddress = "Hanoi, Vietnam",
                note = "Giao gio hanh chinh",
                usedRewardPoints = 500,
                items = listOf(PaymentLineItem(foodId = "food-1", quantity = 3)),
                voucherCode = "VOUCHER10",
                accessToken = "token-123"
            )
        )

        assertEquals("customer-123", json.getString("p_customer_id"))
        assertEquals(90_000L, json.getLong("p_amount"))
        assertEquals("WALLET", json.getString("p_method"))
        assertEquals("ZALOPAY", json.getString("p_provider"))
        assertEquals("ZP-123", json.getString("p_transaction_id"))
        assertEquals("Hanoi, Vietnam", json.getString("p_delivery_address"))
        assertEquals("Giao gio hanh chinh", json.getString("p_note"))
        assertEquals(500, json.getInt("p_used_reward_points"))
        assertEquals("VOUCHER10", json.getString("p_voucher_code"))

        val items = json.getJSONArray("p_items")
        assertEquals(1, items.length())
        val item = items.getJSONObject(0)
        assertEquals("food-1", item.getString("food_id"))
        assertEquals(3, item.getInt("quantity"))
    }

    @Test
    fun toRpcBody_handlesNullsWithJsonObjectNull() {
        val json = PaymentRpcMapper.toRpcBody(
            AtomicPaymentRequest(
                customerId = "customer-123",
                amount = 0L,
                method = PaymentMethod.COD,
                provider = null,
                transactionId = null,
                deliveryAddress = "Hanoi, Vietnam",
                note = null,
                usedRewardPoints = 0,
                items = listOf(PaymentLineItem(foodId = "food-1", quantity = 1)),
                voucherCode = null,
                accessToken = "token-123"
            )
        )

        assertTrue(json.isNull("p_provider"))
        assertTrue(json.isNull("p_transaction_id"))
        assertTrue(json.isNull("p_note"))
        assertTrue(json.isNull("p_voucher_code"))
    }
}

