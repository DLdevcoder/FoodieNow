package com.example.foodienow.feature.food_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.ReviewUiModel
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.repository.CustomerFoodRepository
import com.example.foodienow.domain.repository.MerchantRepository
import com.example.foodienow.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodDetailUiState(
    val isLoading: Boolean = true,
    val food: Food? = null,
    val store: Store? = null,
    val reviews: List<ReviewUiModel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: CustomerFoodRepository,
    private val merchantRepository: MerchantRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: com.example.foodienow.domain.repository.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    private val foodId: String = checkNotNull(savedStateHandle["foodId"])

    init {
        loadFoodDetail()
    }

    private fun loadFoodDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val food = foodRepository.getFoodById(foodId)
                val store = merchantRepository.getStoreById(food.storeId)
                val reviews = reviewRepository.getReviewsByFoodId(foodId) // Vẫn fetch data

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        food = food,
                        store = store,
                        reviews = reviews
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Lỗi tải dữ liệu")
                }
            }
        }
    }

}