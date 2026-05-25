package com.example.foodienow.domain.payment

import com.example.foodienow.domain.model.WalletProvider

interface WalletPaymentGateway {
    suspend fun charge(
        provider: WalletProvider,
        amount: Long,
        orderId: String,
        customerId: String
    ): Result<WalletChargeResult>

    suspend fun withdraw(
        provider: WalletProvider,
        amount: Long,
        transactionId: String,
        customerId: String
    ): Result<WalletWithdrawResult>
}

data class WalletChargeResult(
    val transactionId: String,
    val message: String,
    val paymentUrl: String? = null
)

data class WalletWithdrawResult(
    val transactionId: String,
    val message: String
)

