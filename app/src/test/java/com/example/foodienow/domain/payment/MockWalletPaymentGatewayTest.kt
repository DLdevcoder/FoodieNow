package com.example.foodienow.domain.payment

import com.example.foodienow.data.payment.MockWalletPaymentGateway
import com.example.foodienow.domain.model.WalletProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockWalletPaymentGatewayTest {

    private val gateway = MockWalletPaymentGateway()

    @Test
    fun charge_successfulForNormalAmount() = runBlocking {
        val result = gateway.charge(
            provider = WalletProvider.MOMO,
            amount = 50000L,
            orderId = "order-123",
            customerId = "customer-123"
        )

        assertTrue(result.isSuccess)
        val chargeResult = result.getOrNull()
        assertTrue(chargeResult != null)
        assertTrue(chargeResult!!.transactionId.startsWith("MOMO-SB-"))
        assertEquals("MoMo Sandbox mock charge success", chargeResult.message)
    }

    @Test
    fun charge_failedForAmountEndingIn99() = runBlocking {
        val result = gateway.charge(
            provider = WalletProvider.ZALOPAY,
            amount = 50099L,
            orderId = "order-123",
            customerId = "customer-123"
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception != null)
        assertTrue(exception!!.message!!.contains("ZALOPAY Sandbox: simulated charge failure"))
    }
}
