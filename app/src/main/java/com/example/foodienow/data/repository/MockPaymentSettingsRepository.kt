package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PaymentSettingsState(
    val defaultMethod: PaymentMethod = PaymentMethod.COD,
    val defaultProvider: WalletProvider = WalletProvider.ZALOPAY
)

@Singleton
class MockPaymentSettingsRepository @Inject constructor() {
    private val _settings = MutableStateFlow(PaymentSettingsState())
    val settings: StateFlow<PaymentSettingsState> = _settings.asStateFlow()

    fun updateSettings(method: PaymentMethod, provider: WalletProvider) {
        _settings.value = PaymentSettingsState(defaultMethod = method, defaultProvider = provider)
    }
}
