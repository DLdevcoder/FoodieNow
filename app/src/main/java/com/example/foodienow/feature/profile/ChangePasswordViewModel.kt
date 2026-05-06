package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun changePassword(newPass: String) {
        if (newPass.length < 6) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu phải có ít nhất 6 ký tự.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = authRepository.changePassword(newPass)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Đổi mật khẩu thành công!"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Lỗi khi đổi mật khẩu."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
