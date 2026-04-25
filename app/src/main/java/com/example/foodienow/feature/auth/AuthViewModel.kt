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
	val currentUser: User? = null,
	val pendingVerificationEmail: String? = null,
	val infoMessage: String? = null,
	val errorMessage: String? = null,
	val failedAttempts: Int = 0,
	val cooldownUntilMillis: Long = 0L,
	val remainingCooldownSeconds: Int = 0
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
					errorMessage = "Dang tam khoa dang nhap. Thu lai sau ${remainingSeconds}s.",
					remainingCooldownSeconds = remainingSeconds
				)
			}
			return
		}

		if (normalizedEmail.isBlank() || pass.isBlank()) {
			_uiState.update { it.copy(errorMessage = "Email va mat khau khong duoc de trong.") }
			return
		}

		if (!EMAIL_REGEX.matches(normalizedEmail)) {
			_uiState.update { it.copy(errorMessage = "Email khong dung dinh dang.") }
			return
		}

		if (pass.length < MIN_PASSWORD_LENGTH) {
			_uiState.update { it.copy(errorMessage = "Mat khau toi thieu $MIN_PASSWORD_LENGTH ky tu.") }
			return
		}

		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
			val result = authRepository.login(normalizedEmail, pass)
			result
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
							System.currentTimeMillis() + COOLDOWN_MILLIS
						} else {
							0L
						}
						val cooldownSeconds = calculateRemainingCooldownSeconds(nextCooldownUntil)
						it.copy(
							isLoading = false,
							errorMessage = if (shouldCooldown) {
								"Ban da dang nhap sai nhieu lan. Thu lai sau ${cooldownSeconds}s."
							} else {
								error.message ?: "Dang nhap that bai."
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
			val result = authRepository.register(email.trim(), pass, role)
			result
				.onSuccess {
					_uiState.update {
						it.copy(
							isLoading = false,
							pendingVerificationEmail = email.trim(),
							infoMessage = "Dang ky thanh cong. Vui long xac thuc email de dang nhap."
						)
					}
				}
				.onFailure { error ->
					_uiState.update {
						it.copy(
							isLoading = false,
							errorMessage = error.message ?: "Dang ky that bai."
						)
					}
				}
		}
	}

	fun resendVerificationEmail(email: String) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
			val result = authRepository.resendVerificationEmail(email.trim())
			result
				.onSuccess {
					_uiState.update {
						it.copy(
							isLoading = false,
							infoMessage = "Da gui lai email xac thuc."
						)
					}
				}
				.onFailure { error ->
					_uiState.update {
						it.copy(
							isLoading = false,
							errorMessage = error.message ?: "Khong gui duoc email xac thuc."
						)
					}
				}
		}
	}

	fun forgotPassword(email: String) {
		val normalizedEmail = email.trim()
		if (normalizedEmail.isBlank()) {
			_uiState.update { it.copy(errorMessage = "Nhap email de khoi phuc mat khau.", infoMessage = null) }
			return
		}

		if (!EMAIL_REGEX.matches(normalizedEmail)) {
			_uiState.update { it.copy(errorMessage = "Email khong dung dinh dang.", infoMessage = null) }
			return
		}

		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
			val result = authRepository.forgotPassword(normalizedEmail)
			result
				.onSuccess {
					_uiState.update {
						it.copy(
							isLoading = false,
							infoMessage = "Da gui email khoi phuc mat khau. Vui long kiem tra hop thu."
						)
					}
				}
				.onFailure { error ->
					_uiState.update {
						it.copy(
							isLoading = false,
							errorMessage = error.message ?: "Khong gui duoc email khoi phuc."
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

	fun clearMessage() {
		_uiState.update { it.copy(infoMessage = null, errorMessage = null) }
	}

	fun refreshCooldownState() {
		_uiState.update {
			val seconds = calculateRemainingCooldownSeconds(it.cooldownUntilMillis)
			it.copy(remainingCooldownSeconds = seconds)
		}
	}

	suspend fun resolveStoredSession(): User? {
		return authRepository.getAuthState().first()
	}

	private fun calculateRemainingCooldownSeconds(untilMillis: Long): Int {
		if (untilMillis <= 0L) return 0
		val remainingMillis = untilMillis - System.currentTimeMillis()
		if (remainingMillis <= 0L) return 0
		return ((remainingMillis + 999) / 1000).toInt()
	}

	companion object {
		private const val MAX_FAILED_ATTEMPTS = 3
		private const val COOLDOWN_MILLIS = 30_000L
		private const val MIN_PASSWORD_LENGTH = 6
		private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
	}
}
