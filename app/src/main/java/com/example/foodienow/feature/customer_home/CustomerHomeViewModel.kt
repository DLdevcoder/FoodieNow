package com.example.foodienow.feature.customer_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.CustomerFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// State giữ nguyên
data class HomeUiState(
    val isLoading: Boolean = false,
    val recommendedFoods: List<Food> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
)

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val foodRepository: CustomerFoodRepository
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
                foodRepository.getRecommendedFoods().collect { realFoods ->
                    _uiState.update {
                        it.copy(isLoading = false, recommendedFoods = realFoods)
                    }
                }
            } catch (e: Exception) {
                // Thất bại (Mất mạng, sai Key Supabase...)
                e.printStackTrace() // In lỗi ra logcat để gỡ rối
                _uiState.update { it.copy(isLoading = false) }
                println("LỖI KẾT NỐI DB: ${e.message}")
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