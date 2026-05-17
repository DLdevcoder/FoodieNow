package com.example.foodienow.feature.customer_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Category
import com.example.foodienow.domain.model.Food
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.domain.repository.CustomerFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val recommendedFoods: List<Food> = emptyList(),
    val categories: List<Category> = emptyList(), // Chứa dữ liệu trả về từ bảng categories
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val address: String = "Chọn địa chỉ giao hàng"
)

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val foodRepository: CustomerFoodRepository,
    private val addressRepository: MockAddressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRealData()
    }

    private fun loadRealData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Tải danh sách category từ Supabase
                val fetchedCategories = foodRepository.getCategories()

                addressRepository.addresses.collect { addresses ->
                    val defaultAddress = addresses.firstOrNull { it.isDefault }?.detail ?: "Chọn địa chỉ giao hàng"

                    foodRepository.getRecommendedFoods().collect { realFoods ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                recommendedFoods = realFoods,
                                categories = fetchedCategories, // Đẩy dữ liệu vào State để UI tự vẽ
                                address = defaultAddress
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { state ->
            val filteredList = if (newQuery.isBlank()) {
                emptyList()
            } else {
                state.recommendedFoods.filter { food ->
                    food.name.contains(newQuery, ignoreCase = true)
                }
            }

            state.copy(
                searchQuery = newQuery,
                searchResults = filteredList
            )
        }
    }
}