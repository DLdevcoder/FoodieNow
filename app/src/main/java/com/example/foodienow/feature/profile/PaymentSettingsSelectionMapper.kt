package com.example.foodienow.feature.profile

import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.PaymentMethodCatalog

internal data class PaymentSettingsSelection(
    val method: PaymentMethod,
    val provider: WalletProvider
)

internal object PaymentSettingsSelectionMapper {
    fun fromOptionId(
        id: String,
        fallbackProvider: WalletProvider
    ): PaymentSettingsSelection? {
        val option = PaymentMethodCatalog.optionFor(id) ?: return null
        return PaymentSettingsSelection(
            method = option.method,
            provider = option.provider ?: fallbackProvider
        )
    }

    fun toOptionId(
        method: PaymentMethod,
        provider: WalletProvider?
    ): String {
        return PaymentMethodCatalog.optionIdFor(method, provider)
    }
}
