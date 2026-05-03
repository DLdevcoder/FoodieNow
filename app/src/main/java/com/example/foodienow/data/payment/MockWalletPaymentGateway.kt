package com.example.foodienow.data.payment

import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.WalletChargeResult
import com.example.foodienow.domain.payment.WalletPaymentGateway
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class MockWalletPaymentGateway @Inject constructor() : WalletPaymentGateway {
    override suspend fun charge(
        provider: WalletProvider,
        amount: Double,
        orderId: String,
        customerId: String
    ): Result<WalletChargeResult> {
        // Simulate Sandbox delay
        delay(2000)
        
        val transactionId = "${provider.name}-SB-${UUID.randomUUID().toString().substring(0, 8)}"
        val message = when (provider) {
            WalletProvider.ZALOPAY -> "ZaloPay Sandbox mock charge success"
            WalletProvider.MOMO -> "MoMo Sandbox mock charge success"
            WalletProvider.VNPAY -> "VNPAY Sandbox mock charge success"
            WalletProvider.PAYPAL -> "PayPal Sandbox mock charge success"
            WalletProvider.GOOGLE_PLAY -> "Google Play Billing Sandbox mock charge success"
        }
        return Result.success(WalletChargeResult(transactionId = transactionId, message = message))
    }
}

