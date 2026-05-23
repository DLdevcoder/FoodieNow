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
    val isSendingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val email: String = "",
    val isCodeSent: Boolean = false,
    val isCodeVerified: Boolean = false,
    val cooldownUntilMillis: Long = 0L,
    val remainingCooldownSeconds: Int = 0,
    val successMessage: String? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    init {
        loadRegisteredEmail()
    }

    private fun loadRegisteredEmail() {
        viewModelScope.launch {
            val user = authRepository.resolveStoredSession()
            _uiState.update {
                it.copy(
                    email = user?.email.orEmpty(),
                    errorMessage = if (user == null) "Không tìm thấy phiên đăng nhập." else null
                )
            }
        }
    }

    fun refreshCooldownState() {
        _uiState.update { state ->
            val seconds = calculateRemainingCooldownSeconds(state.cooldownUntilMillis)
            state.copy(remainingCooldownSeconds = seconds)
        }
    }

    fun sendVerificationCode() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Không tìm thấy email đăng ký.") }
            return
        }

        val remainingSeconds = calculateRemainingCooldownSeconds(_uiState.value.cooldownUntilMillis)
        if (remainingSeconds > 0) {
            _uiState.update {
                it.copy(
                    errorMessage = "Vui lòng đợi ${remainingSeconds}s trước khi gửi lại mã.",
                    remainingCooldownSeconds = remainingSeconds
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingCode = true,
                    infoMessage = null,
                    errorMessage = null,
                    successMessage = null
                )
            }

            authRepository.sendPasswordChangeCode(email)
                .onSuccess {
                    val nextCooldownUntil = System.currentTimeMillis() + COOLDOWN_MILLIS
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            isCodeSent = true,
                            isCodeVerified = false,
                            cooldownUntilMillis = nextCooldownUntil,
                            remainingCooldownSeconds = calculateRemainingCooldownSeconds(nextCooldownUntil),
                            infoMessage = "Đã gửi mã xác nhận tới email đăng ký."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            errorMessage = error.message ?: "Không gửi được mã xác nhận."
                        )
                    }
                }
        }
    }

    fun verifyCode(code: String) {
        val email = _uiState.value.email.trim()
        val normalizedCode = code.filterNot(Char::isWhitespace)

        when {
            email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Không tìm thấy email đăng ký.") }
                return
            }
            normalizedCode.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Vui lòng nhập mã xác nhận từ email.") }
                return
            }
            normalizedCode.length != VERIFICATION_CODE_LENGTH -> {
                _uiState.update { it.copy(errorMessage = "Mã xác nhận gồm $VERIFICATION_CODE_LENGTH ký tự.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isVerifyingCode = true,
                    infoMessage = null,
                    errorMessage = null,
                    successMessage = null
                )
            }

            authRepository.verifyPasswordChangeCode(email, normalizedCode)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            isCodeVerified = true,
                            infoMessage = "Mã xác nhận hợp lệ. Bạn có thể đặt mật khẩu mới."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            isCodeVerified = false,
                            errorMessage = error.message ?: "Mã xác nhận không hợp lệ hoặc đã hết hạn."
                        )
                    }
                }
        }
    }

    fun changePassword(newPass: String, confirmPass: String) {
        if (!_uiState.value.isCodeVerified) {
            _uiState.update { it.copy(errorMessage = "Vui lòng xác minh mã email trước.") }
            return
        }

        when {
            newPass.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Mật khẩu phải có ít nhất 6 ký tự.") }
                return
            }
            newPass != confirmPass -> {
                _uiState.update { it.copy(errorMessage = "Mật khẩu xác nhận không khớp.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    infoMessage = null,
                    errorMessage = null,
                    successMessage = null
                )
            }

            authRepository.changePassword(newPass)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Đổi mật khẩu thành công."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Không đổi được mật khẩu."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null, successMessage = null) }
    }

    private fun calculateRemainingCooldownSeconds(untilMillis: Long): Int {
        if (untilMillis <= 0L) return 0
        val remainingMillis = untilMillis - System.currentTimeMillis()
        if (remainingMillis <= 0L) return 0
        return ((remainingMillis + 999) / 1000).toInt()
    }

    companion object {
        private const val COOLDOWN_MILLIS = 30_000L
        private const val VERIFICATION_CODE_LENGTH = 8
    }
}
