package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import com.example.foodienow.data.repository.MockPaymentSettingsRepository
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PaymentSettingsViewModel @Inject constructor(
    private val paymentSettingsRepository: MockPaymentSettingsRepository
) : ViewModel() {
    val settings = paymentSettingsRepository.settings
    
    fun updateDefaultMethod(id: String) {
        when (id) {
            "momo" -> paymentSettingsRepository.updateSettings(PaymentMethod.WALLET, WalletProvider.MOMO)
            "zalopay" -> paymentSettingsRepository.updateSettings(PaymentMethod.WALLET, WalletProvider.ZALOPAY)
            "card" -> paymentSettingsRepository.updateSettings(PaymentMethod.CARD, WalletProvider.ZALOPAY)
            "cod" -> paymentSettingsRepository.updateSettings(PaymentMethod.COD, WalletProvider.ZALOPAY)
        }
    }
}
