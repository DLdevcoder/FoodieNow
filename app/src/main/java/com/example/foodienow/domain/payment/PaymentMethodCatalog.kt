package com.example.foodienow.domain.payment

import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider

data class PaymentMethodOption(
    val id: String,
    val method: PaymentMethod,
    val provider: WalletProvider? = null,
    val requiresSetup: Boolean
)

object PaymentMethodCatalog {
    const val COD_ID = "cod"
    const val FOODIE_PAY_ID = "foodie_pay"
    const val MOMO_ID = "momo"
    const val ZALOPAY_ID = "zalopay"
    const val VNPAY_ID = "vnpay"
    const val PAYPAL_ID = "paypal"

    val allOptions: List<PaymentMethodOption> = listOf(
        PaymentMethodOption(COD_ID, PaymentMethod.COD, requiresSetup = false),
        PaymentMethodOption(FOODIE_PAY_ID, PaymentMethod.FOODIE_PAY, requiresSetup = false),
        PaymentMethodOption(MOMO_ID, PaymentMethod.WALLET, WalletProvider.MOMO, requiresSetup = true),
        PaymentMethodOption(ZALOPAY_ID, PaymentMethod.WALLET, WalletProvider.ZALOPAY, requiresSetup = true),
        PaymentMethodOption(VNPAY_ID, PaymentMethod.WALLET, WalletProvider.VNPAY, requiresSetup = true),
        PaymentMethodOption(PAYPAL_ID, PaymentMethod.WALLET, WalletProvider.PAYPAL, requiresSetup = true)
    )

    val alwaysAvailableOptionIds: Set<String> = allOptions
        .filterNot { it.requiresSetup }
        .map { it.id }
        .toSet()

    fun optionFor(id: String): PaymentMethodOption? {
        return allOptions.firstOrNull { it.id == id }
    }

    fun optionIdFor(method: PaymentMethod, provider: WalletProvider?): String {
        return when (method) {
            PaymentMethod.COD -> COD_ID
            PaymentMethod.FOODIE_PAY -> FOODIE_PAY_ID
            PaymentMethod.WALLET -> when (provider) {
                WalletProvider.MOMO -> MOMO_ID
                WalletProvider.ZALOPAY -> ZALOPAY_ID
                WalletProvider.VNPAY -> VNPAY_ID
                WalletProvider.PAYPAL -> PAYPAL_ID
                null -> ZALOPAY_ID
            }
        }
    }

    fun isAlwaysAvailable(optionId: String): Boolean {
        return optionId in alwaysAvailableOptionIds
    }

    fun isOptionAvailable(optionId: String, configuredOptionIds: Set<String>): Boolean {
        return isAlwaysAvailable(optionId) || optionId in configuredOptionIds
    }
}
