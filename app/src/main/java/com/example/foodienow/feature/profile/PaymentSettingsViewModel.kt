package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.PaymentSettingsRepository
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
        viewModelScope.launch {
            val currentProvider = paymentSettingsRepository.settings.value.defaultProvider
            val selection = PaymentSettingsSelectionMapper.fromOptionId(id, currentProvider)
                ?: return@launch

            paymentSettingsRepository.updateSettings(
                method = selection.method,
                provider = selection.provider
            )
        }
    }
}
