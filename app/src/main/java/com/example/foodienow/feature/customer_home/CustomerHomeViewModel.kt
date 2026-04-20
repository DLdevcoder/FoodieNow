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
    val searchQuery: String = ""
)

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    // TIÊM (INJECT) REPOSITORY VÀO ĐÂY
    private val foodRepository: CustomerFoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRealData() // Vừa vào màn hình là gọi data thật luôn
    }

    private fun loadRealData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Bật loading xoay xoay

            try {
                // Gọi hàm lấy dữ liệu từ Supabase và lắng nghe kết quả (collect)
                foodRepository.getRecommendedFoods().collect { realFoods ->
                    // Thành công: Đưa list món ăn lấy được vào State để UI tự vẽ lại
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
        _uiState.update { it.copy(searchQuery = newQuery) }
    }
}