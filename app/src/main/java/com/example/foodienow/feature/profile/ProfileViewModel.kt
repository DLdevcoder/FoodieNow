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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val user: User? = null,
    val profile: Profile? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null
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

            val sessionUser = authRepository.getAuthState().first()
            if (sessionUser == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = null,
                        profile = null,
                        errorMessage = "Khong tim thay phien dang nhap."
                    )
                }
                return@launch
            }

            val existingProfile = runCatching {
                profileRepository.getProfile(sessionUser.id).first()
            }.getOrNull()

            val profile = existingProfile ?: Profile(
                id = sessionUser.id,
                email = sessionUser.email,
                fullName = sessionUser.name,
                role = sessionUser.role
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = sessionUser,
                    profile = profile
                )
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

    fun onAddressChange(value: String) {
        _uiState.update { state ->
            state.copy(
                profile = state.profile?.copy(address = value),
                infoMessage = null,
                errorMessage = null
            )
        }
    }

    fun saveProfile() {
        val profile = _uiState.value.profile ?: return
        if (profile.fullName.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Ten khong duoc de trong.", infoMessage = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, infoMessage = null, errorMessage = null) }
            profileRepository.upsertProfile(profile)
                .onSuccess { savedProfile ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            profile = savedProfile,
                            infoMessage = "Da luu thong tin ho so."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Khong luu duoc ho so."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}

