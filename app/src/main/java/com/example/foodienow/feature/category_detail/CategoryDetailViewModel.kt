package com.example.foodienow.feature.category_detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.CustomerFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryDetailUiState(
    val isLoading: Boolean = true,
    val categoryName: String = "",
    val foods: List<Food> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: CustomerFoodRepository
) : ViewModel() {

    // Lấy params từ Navigation route
    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""
    private val categoryName: String = savedStateHandle.get<String>("categoryName")?.let(Uri::decode) ?: "Danh mục"

    private val _uiState = MutableStateFlow(CategoryDetailUiState(categoryName = categoryName))
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadFoods()
    }

    fun refresh() {
        loadFoods()
    }

    private fun loadFoods() {
        if (categoryId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy danh mục") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            foodRepository.getFoodsByCategory(categoryId)
                .catch { e ->
                    e.printStackTrace()
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Lỗi tải dữ liệu: ${e.message}")
                    }
                }
                .collect { foodsList ->
                    _uiState.update {
                        it.copy(isLoading = false, foods = foodsList)
                    }
                }
        }
    }
}
