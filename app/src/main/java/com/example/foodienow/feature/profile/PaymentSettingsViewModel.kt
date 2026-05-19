package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.PaymentSettingsRepository
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentSettingsViewModel @Inject constructor(
    private val paymentSettingsRepository: PaymentSettingsRepository
) : ViewModel() {
    val settings = paymentSettingsRepository.settings

    init {
        viewModelScope.launch {
            paymentSettingsRepository.refreshSettings()
        }
    }
    
    fun updateDefaultMethod(id: String) {
        val (method, provider) = when (id) {
            "momo" -> PaymentMethod.WALLET to WalletProvider.MOMO
            "zalopay" -> PaymentMethod.WALLET to WalletProvider.ZALOPAY
            "card" -> PaymentMethod.CARD to WalletProvider.ZALOPAY
            "cod" -> PaymentMethod.COD to WalletProvider.ZALOPAY
            else -> return
        }

        viewModelScope.launch {
            paymentSettingsRepository.updateSettings(method, provider)
        }
    }
}
