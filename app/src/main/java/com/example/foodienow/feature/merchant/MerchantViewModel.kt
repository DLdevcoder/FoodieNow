package com.example.foodienow.feature.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.MerchantRepository
import com.example.foodienow.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MerchantUiState(
    val isLoading: Boolean = false,
    val store: Store? = null,
    val menu: List<Food> = emptyList(),
    val error: String? = null,
    val unreadMessageCount: Int = 0
)

@HiltViewModel
class MerchantViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val merchantRepository: MerchantRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MerchantUiState())
    val uiState: StateFlow<MerchantUiState> = _uiState.asStateFlow()

    init {
        loadMerchantData()
        loadUnreadMessageCount()
    }

    private fun loadMerchantData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()

                if (currentUser != null) {
                    val store = merchantRepository.getStoreByOwnerId(currentUser.id)

                    if (store != null) {
                        _uiState.update { it.copy(store = store) }
                        merchantRepository.getMerchantMenu(store.id).collect { foodList ->
                            _uiState.update { it.copy(menu = foodList, isLoading = false) }
                        }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, error = "Không tìm thấy thông tin cửa hàng")
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Chưa đăng nhập")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadUnreadMessageCount() {
        viewModelScope.launch {
            try {
                val user = authRepository.resolveStoredSession()
                val currentUserId = user?.id ?: return@launch
                val count = chatRepository.getTotalUnreadCount(currentUserId)
                _uiState.update { it.copy(unreadMessageCount = count) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFoodAvailability(food: Food) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedMenu = currentState.menu.map { currentItem ->
                    if (currentItem.id == food.id) {
                        currentItem.copy(isAvailable = !currentItem.isAvailable)
                    } else {
                        currentItem
                    }
                }
                currentState.copy(menu = updatedMenu)
            }

            try {
                val updatedFood = food.copy(isAvailable = !food.isAvailable)
                merchantRepository.updateFood(updatedFood)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateStoreInfo(
        newName: String,
        newAddress: String,
        newOpeningTime: String,
        newClosingTime: String,
        newIsActive: Boolean,
        imageBytes: ByteArray?
    ) {
        val currentStore = _uiState.value.store ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val tempStore = currentStore.copy(
                name = newName,
                address = newAddress.takeIf { it.isNotBlank() },
                openingTime = newOpeningTime.takeIf { it.isNotBlank() },
                closingTime = newClosingTime.takeIf { it.isNotBlank() },
                isActive = newIsActive
            )

            val result = merchantRepository.updateStore(tempStore, imageBytes)

            if (result.isSuccess) {
                try {
                    val updatedStore = merchantRepository.getStoreById(currentStore.id)
                    _uiState.update { it.copy(store = updatedStore, isLoading = false) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = "Lỗi khi tải lại dữ liệu") }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi cập nhật cửa hàng"
                    )
                }
            }
        }
    }
}