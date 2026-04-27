package com.example.foodienow.feature.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.MerchantRepository
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
    val error: String? = null
)

@HiltViewModel
class MerchantViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val merchantRepository: MerchantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MerchantUiState())
    val uiState: StateFlow<MerchantUiState> = _uiState.asStateFlow()

    init {
        loadMerchantData()
    }

    private fun loadMerchantData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Lấy giá trị user hiện tại từ Flow
                val currentUser = authRepository.getAuthState().firstOrNull()

                if (currentUser != null) {
                    val store = merchantRepository.getStoreByOwnerId(currentUser.id)

                    if (store != null) {
                        _uiState.update { it.copy(store = store) }

                        // Lấy thực đơn của quán và lắng nghe thay đổi
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

    fun toggleFoodAvailability(food: Food) {
        viewModelScope.launch {
            val updatedFood = food.copy(isAvailable = !food.isAvailable)
            merchantRepository.updateFood(updatedFood)
        }
    }
}