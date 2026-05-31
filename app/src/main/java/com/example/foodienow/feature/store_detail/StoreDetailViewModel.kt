package com.example.foodienow.feature.store_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.repository.CustomerFoodRepository
import com.example.foodienow.domain.repository.MerchantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoreDetailUiState(
    val isLoading: Boolean = true,
    val store: Store? = null,
    val foods: List<Food> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: CustomerFoodRepository,
    private val merchantRepository: MerchantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreDetailUiState())
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    val storeId: String = checkNotNull(savedStateHandle["storeId"])

    init {
        loadStoreDetail()
    }

    fun loadStoreDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val store = merchantRepository.getStoreById(storeId)
                val foods = foodRepository.getFoodsByStoreId(storeId)
                val sortedFoods = foods.sortedByDescending { it.soldCount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        store = store,
                        foods = sortedFoods
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Không thể tải thông tin cửa hàng"
                    )
                }
            }
        }
    }
}
