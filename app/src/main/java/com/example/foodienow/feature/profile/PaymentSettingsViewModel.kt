package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.PaymentSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentSettingsUiState(
    val isSaving: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PaymentSettingsViewModel @Inject constructor(
    private val paymentSettingsRepository: PaymentSettingsRepository
) : ViewModel() {
    val settings = paymentSettingsRepository.settings
    private val _uiState = MutableStateFlow(PaymentSettingsUiState())
    val uiState: StateFlow<PaymentSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            paymentSettingsRepository.refreshSettings()
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Khong the tai cai dat thanh toan.")
                    }
                }
        }
    }
    
    fun updateDefaultMethod(id: String) {
        runSettingsAction(successMessage = "Da cap nhat phuong thuc mac dinh.") {
            paymentSettingsRepository.updateDefaultMethod(id)
        }
    }

    fun savePaymentMethodInfo(
        optionId: String,
        displayName: String,
        details: String
    ) {
        runSettingsAction(successMessage = "Da luu thong tin thanh toan.") {
            paymentSettingsRepository.savePaymentMethodInfo(
                optionId = optionId,
                displayName = displayName,
                details = details
            )
        }
    }

    fun removePaymentMethodInfo(optionId: String) {
        runSettingsAction(successMessage = "Da go thong tin thanh toan.") {
            paymentSettingsRepository.removePaymentMethodInfo(optionId)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }

    private fun runSettingsAction(
        successMessage: String,
        block: suspend () -> Result<*>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, infoMessage = null, errorMessage = null) }
            block()
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, infoMessage = successMessage, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            infoMessage = null,
                            errorMessage = error.message ?: "Khong the cap nhat cai dat thanh toan."
                        )
                    }
                }
        }
    }
}
