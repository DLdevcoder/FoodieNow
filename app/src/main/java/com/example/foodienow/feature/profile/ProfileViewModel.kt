package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Profile
import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isAvatarSaving: Boolean = false,
    val isLoggingOut: Boolean = false,
    val user: User? = null,
    val profile: Profile? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val closeEditAfterInfo: Boolean = false,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }

            try {
                val sessionUser = authRepository.getAuthState().firstOrNull()
                if (sessionUser == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = null,
                            profile = null,
                            errorMessage = "Không tìm thấy phiên đăng nhập."
                        )
                    }
                } else {
                    val existingProfile = profileRepository.getProfile(sessionUser.id).firstOrNull()
                    val profile = existingProfile ?: Profile(
                        id = sessionUser.id,
                        email = sessionUser.email,
                        fullName = sessionUser.name,
                        role = sessionUser.role,
                        balance = sessionUser.balance,
                        rewardPoints = sessionUser.rewardPoints
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = sessionUser,
                            profile = profile
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể tải hồ sơ."
                    )
                }
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { state ->
            state.copy(
                profile = state.profile?.copy(fullName = value),
                infoMessage = null,
                errorMessage = null
            )
        }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { state ->
            state.copy(
                profile = state.profile?.copy(phone = value),
                infoMessage = null,
                errorMessage = null
            )
        }
    }

    fun uploadAvatar(imageBytes: ByteArray) {
        val profile = _uiState.value.profile ?: return
        if (imageBytes.isEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ảnh đã chọn không hợp lệ.",
                    infoMessage = null,
                    closeEditAfterInfo = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAvatarSaving = true,
                    infoMessage = null,
                    errorMessage = null,
                    closeEditAfterInfo = false
                )
            }

            profileRepository.uploadAvatar(profile, imageBytes)
                .onSuccess { savedProfile ->
                    _uiState.update {
                        it.copy(
                            isAvatarSaving = false,
                            profile = savedProfile,
                            infoMessage = "Đã cập nhật ảnh đại diện.",
                            closeEditAfterInfo = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isAvatarSaving = false,
                            errorMessage = error.message ?: "Không cập nhật được ảnh đại diện."
                        )
                    }
                }
        }
    }

    fun deleteAvatar() {
        val profile = _uiState.value.profile ?: return
        if (profile.avatarUrl.isNullOrBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAvatarSaving = true,
                    infoMessage = null,
                    errorMessage = null,
                    closeEditAfterInfo = false
                )
            }

            profileRepository.deleteAvatar(profile)
                .onSuccess { savedProfile ->
                    _uiState.update {
                        it.copy(
                            isAvatarSaving = false,
                            profile = savedProfile,
                            infoMessage = "Đã xóa ảnh đại diện.",
                            closeEditAfterInfo = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isAvatarSaving = false,
                            errorMessage = error.message ?: "Không xóa được ảnh đại diện."
                        )
                    }
                }
        }
    }

    fun saveProfile() {
        val profile = _uiState.value.profile ?: return
        if (profile.fullName.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Tên không được để trống.", infoMessage = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    infoMessage = null,
                    errorMessage = null,
                    closeEditAfterInfo = false
                )
            }
            profileRepository.upsertProfile(profile)
                .onSuccess { savedProfile ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            profile = savedProfile,
                            infoMessage = "Đã lưu thông tin hồ sơ.",
                            closeEditAfterInfo = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Không lưu được hồ sơ."
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, errorMessage = null, infoMessage = null) }
            val startedAt = System.currentTimeMillis()
            val result = authRepository.logout()
            val remainingMillis = MIN_LOGOUT_SCREEN_MILLIS - (System.currentTimeMillis() - startedAt)
            if (remainingMillis > 0) {
                delay(remainingMillis)
            }
            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            isLoggedOut = true,
                            user = null,
                            profile = null,
                            errorMessage = null,
                            infoMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            errorMessage = error.message ?: "Không thể đăng xuất."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null, closeEditAfterInfo = false) }
    }

    companion object {
        private const val MIN_LOGOUT_SCREEN_MILLIS = 900L
    }
}
