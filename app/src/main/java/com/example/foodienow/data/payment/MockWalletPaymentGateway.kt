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
        delay(600)
        val transactionId = "${provider.name}-${UUID.randomUUID()}"
        val message = when (provider) {
            WalletProvider.ZALOPAY -> "ZaloPay mock charge success"
            WalletProvider.MOMO -> "MoMo mock charge success"
        }
        return Result.success(WalletChargeResult(transactionId = transactionId, message = message))
    }
}

