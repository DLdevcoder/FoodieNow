package com.example.foodienow.feature.profile

import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider

internal data class PaymentSettingsSelection(
    val method: PaymentMethod,
    val provider: WalletProvider
)

internal object PaymentSettingsSelectionMapper {
    fun fromOptionId(
        id: String,
        fallbackProvider: WalletProvider
    ): PaymentSettingsSelection? {
        return when (id) {
            "momo" -> PaymentSettingsSelection(PaymentMethod.WALLET, WalletProvider.MOMO)
            "zalopay" -> PaymentSettingsSelection(PaymentMethod.WALLET, WalletProvider.ZALOPAY)
            "vnpay" -> PaymentSettingsSelection(PaymentMethod.WALLET, WalletProvider.VNPAY)
            "paypal" -> PaymentSettingsSelection(PaymentMethod.WALLET, WalletProvider.PAYPAL)
            "google_play" -> PaymentSettingsSelection(PaymentMethod.WALLET, WalletProvider.GOOGLE_PLAY)
            "foodie_pay" -> PaymentSettingsSelection(PaymentMethod.FOODIE_PAY, fallbackProvider)
            "card" -> PaymentSettingsSelection(PaymentMethod.CARD, fallbackProvider)
            "cod" -> PaymentSettingsSelection(PaymentMethod.COD, fallbackProvider)
            else -> null
        }
    }

    fun toOptionId(
        method: PaymentMethod,
        provider: WalletProvider?
    ): String {
        return when (method) {
            PaymentMethod.WALLET -> when (provider) {
                WalletProvider.MOMO -> "momo"
                WalletProvider.ZALOPAY -> "zalopay"
                WalletProvider.VNPAY -> "vnpay"
                WalletProvider.PAYPAL -> "paypal"
                WalletProvider.GOOGLE_PLAY -> "google_play"
                null -> "zalopay"
            }
            PaymentMethod.FOODIE_PAY -> "foodie_pay"
            PaymentMethod.CARD -> "card"
            PaymentMethod.COD -> "cod"
        }
    }
}
