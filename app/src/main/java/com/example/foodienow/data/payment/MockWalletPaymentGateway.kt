package com.example.foodienow.data.payment

import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.WalletChargeResult
import com.example.foodienow.domain.payment.WalletPaymentGateway
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

/**
 * Mock implementation of [WalletPaymentGateway] for sandbox/testing.
 *
 * Failure simulation: if the amount ends with 99 (e.g. 50099),
 * the charge will fail. This lets testers exercise the error /
 * rollback flow without needing a real payment provider.
 */
class MockWalletPaymentGateway @Inject constructor() : WalletPaymentGateway {
    override suspend fun charge(
        provider: WalletProvider,
        amount: Long,
        orderId: String,
        customerId: String
    ): Result<WalletChargeResult> {
        // Simulate network / gateway delay
        delay(2000)

        // --- Failure simulation ---
        // Amounts ending in 99 trigger a simulated failure so the app's
        // error-handling / rollback path can be tested end-to-end.
        if (amount % 100 == 99L) {
            return Result.failure(
                Exception("${provider.name} Sandbox: simulated charge failure (amount ends in 99)")
            )
        }

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
