package com.example.foodienow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val currentUser: User? = null,
    val pendingVerificationEmail: String? = null,
    val isRegistrationVerified: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val failedAttempts: Int = 0,
    val cooldownUntilMillis: Long = 0L,
    val remainingCooldownSeconds: Int = 0,
    val verificationCooldownUntilMillis: Long = 0L,
    val verificationRemainingCooldownSeconds: Int = 0
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        val normalizedEmail = email.trim()
        val remainingSeconds = calculateRemainingCooldownSeconds(_uiState.value.cooldownUntilMillis)
        if (remainingSeconds > 0) {
            _uiState.update {
                it.copy(
                    errorMessage = "Đăng nhập đang tạm khóa. Thử lại sau ${remainingSeconds}s.",
                    remainingCooldownSeconds = remainingSeconds
                )
            }
            return
        }

        if (normalizedEmail.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email và mật khẩu không được để trống.") }
            return
        }

        if (!EMAIL_REGEX.matches(normalizedEmail)) {
            _uiState.update { it.copy(errorMessage = "Email chưa đúng định dạng.") }
            return
        }

        if (pass.length < MIN_PASSWORD_LENGTH) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu tối thiểu $MIN_PASSWORD_LENGTH ký tự.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.login(normalizedEmail, pass)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            failedAttempts = 0,
                            cooldownUntilMillis = 0L,
                            remainingCooldownSeconds = 0
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        val nextFailedAttempts = it.failedAttempts + 1
                        val shouldCooldown = nextFailedAttempts >= MAX_FAILED_ATTEMPTS
                        val nextCooldownUntil = if (shouldCooldown) {
                            System.currentTimeMillis() + LOGIN_COOLDOWN_MILLIS
                        } else {
                            0L
                        }
                        val cooldownSeconds = calculateRemainingCooldownSeconds(nextCooldownUntil)
                        it.copy(
                            isLoading = false,
                            errorMessage = if (shouldCooldown) {
                                "Bạn đã đăng nhập sai nhiều lần. Thử lại sau ${cooldownSeconds}s."
                            } else {
                                error.toLoginErrorMessage()
                            },
                            failedAttempts = if (shouldCooldown) 0 else nextFailedAttempts,
                            cooldownUntilMillis = nextCooldownUntil,
                            remainingCooldownSeconds = cooldownSeconds
                        )
                    }
                }
        }
    }

    fun register(email: String, pass: String, role: UserRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.register(email.trim(), pass, role)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingVerificationEmail = email.trim(),
                            infoMessage = "Đăng ký thành công. Vui lòng nhập mã xác thực email."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Đăng ký thất bại."
                        )
                    }
                }
        }
    }

    fun startVerificationCooldown() {
        if (_uiState.value.verificationRemainingCooldownSeconds > 0) return
        val cooldownUntil = System.currentTimeMillis() + VERIFICATION_COOLDOWN_MILLIS
        _uiState.update {
            it.copy(
                verificationCooldownUntilMillis = cooldownUntil,
                verificationRemainingCooldownSeconds = calculateRemainingCooldownSeconds(cooldownUntil)
            )
        }
    }

    fun resendVerificationEmail(email: String) {
        val normalizedEmail = email.trim()
        val remainingSeconds = calculateRemainingCooldownSeconds(_uiState.value.verificationCooldownUntilMillis)
        if (remainingSeconds > 0) {
            _uiState.update {
                it.copy(
                    errorMessage = "Vui lòng đợi ${remainingSeconds}s trước khi gửi lại mã.",
                    verificationRemainingCooldownSeconds = remainingSeconds
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.resendVerificationEmail(normalizedEmail)
                .onSuccess {
                    val cooldownUntil = System.currentTimeMillis() + VERIFICATION_COOLDOWN_MILLIS
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            verificationCooldownUntilMillis = cooldownUntil,
                            verificationRemainingCooldownSeconds = calculateRemainingCooldownSeconds(cooldownUntil),
                            infoMessage = "Đã gửi lại mã xác thực."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Không gửi được mã xác thực."
                        )
                    }
                }
        }
    }

    fun verifyRegistrationCode(email: String, code: String) {
        val normalizedEmail = email.trim()
        val normalizedCode = code.filterNot(Char::isWhitespace)
        if (normalizedCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập mã xác thực từ email.") }
            return
        }
        if (normalizedCode.length != VERIFICATION_CODE_LENGTH) {
            _uiState.update { it.copy(errorMessage = "Mã xác thực gồm $VERIFICATION_CODE_LENGTH ký tự.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true, errorMessage = null, infoMessage = null) }
            authRepository.verifyRegistrationCode(normalizedEmail, normalizedCode)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            isRegistrationVerified = true,
                            infoMessage = "Xác thực tài khoản thành công. Bạn có thể đăng nhập."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            errorMessage = error.message ?: "Mã xác thực không hợp lệ hoặc đã hết hạn."
                        )
                    }
                }
        }
    }

    fun forgotPassword(email: String) {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nhập email để khôi phục mật khẩu.", infoMessage = null) }
            return
        }

        if (!EMAIL_REGEX.matches(normalizedEmail)) {
            _uiState.update { it.copy(errorMessage = "Email chưa đúng định dạng.", infoMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.forgotPassword(normalizedEmail)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Đã gửi email khôi phục mật khẩu. Vui lòng kiểm tra hộp thư."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Không gửi được email khôi phục."
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    currentUser = null,
                    pendingVerificationEmail = null,
                    infoMessage = null,
                    errorMessage = null
                )
            }
        }
    }

    fun consumeLoginState() {
        _uiState.update { it.copy(currentUser = null) }
    }

    fun consumeVerificationTarget() {
        _uiState.update { it.copy(pendingVerificationEmail = null) }
    }

    fun consumeRegistrationVerified() {
        _uiState.update { it.copy(isRegistrationVerified = false) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }

    fun refreshCooldownState() {
        _uiState.update {
            val seconds = calculateRemainingCooldownSeconds(it.cooldownUntilMillis)
            it.copy(remainingCooldownSeconds = seconds)
        }
    }

    fun refreshVerificationCooldownState() {
        _uiState.update {
            val seconds = calculateRemainingCooldownSeconds(it.verificationCooldownUntilMillis)
            it.copy(verificationRemainingCooldownSeconds = seconds)
        }
    }

    suspend fun resolveStoredSession(): User? {
        return authRepository.getAuthState().first()
    }

    private fun Throwable.toLoginErrorMessage(): String {
        val message = message.orEmpty().lowercase()
        return if (
            message.contains("invalid login credentials") ||
            message.contains("invalid credentials") ||
            message.contains("email not found") ||
            message.contains("password")
        ) {
            "Sai tài khoản hoặc mật khẩu."
        } else {
            this.message ?: "Đăng nhập thất bại."
        }
    }

    private fun calculateRemainingCooldownSeconds(untilMillis: Long): Int {
        if (untilMillis <= 0L) return 0
        val remainingMillis = untilMillis - System.currentTimeMillis()
        if (remainingMillis <= 0L) return 0
        return ((remainingMillis + 999) / 1000).toInt()
    }

    companion object {
        private const val MAX_FAILED_ATTEMPTS = 3
        private const val LOGIN_COOLDOWN_MILLIS = 30_000L
        private const val VERIFICATION_COOLDOWN_MILLIS = 30_000L
        private const val MIN_PASSWORD_LENGTH = 6
        private const val VERIFICATION_CODE_LENGTH = 8
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}
