package com.example.foodienow.domain.payment

import com.example.foodienow.domain.model.WalletProvider

interface WalletPaymentGateway {
    suspend fun charge(
        provider: WalletProvider,
        amount: Long,
        orderId: String,
        customerId: String
    ): Result<WalletChargeResult>
}

data class WalletChargeResult(
    val transactionId: String,
    val message: String,
    val paymentUrl: String? = null
)

