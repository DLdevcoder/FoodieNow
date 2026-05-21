package com.example.foodienow.feature.customer_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Category
import com.example.foodienow.domain.model.Food
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.domain.repository.CustomerFoodRepository
// THÊM: Import Repository cần thiết cho tính năng Chat
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.ChatRepository
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
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val address: String = "Chọn địa chỉ giao hàng",
    // THÊM: Thuộc tính lưu số lượng tin nhắn chưa đọc
    val unreadMessageCount: Int = 0
)

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val foodRepository: CustomerFoodRepository,
    private val addressRepository: MockAddressRepository,
    // THÊM: Inject ChatRepository và AuthRepository
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRealData()
        loadUnreadMessageCount()
    }

    private fun loadRealData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val fetchedCategories = foodRepository.getCategories()

                addressRepository.addresses.collect { addresses ->
                    val defaultAddress = addresses.firstOrNull { it.isDefault }?.detail ?: "Chọn địa chỉ giao hàng"

                    foodRepository.getRecommendedFoods().collect { realFoods ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                recommendedFoods = realFoods,
                                categories = fetchedCategories,
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

    // tải số lượng tin nhắn chưa đọc từ Supabase
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