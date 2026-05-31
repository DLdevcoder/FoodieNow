package com.example.foodienow.feature.customer_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.domain.model.Category
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.repository.CustomerFoodRepository
// THÊM: Import Repository cần thiết cho tính năng Chat
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.ChatRepository
import com.example.foodienow.domain.repository.MerchantRepository
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
    val featuredStores: List<Store> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val address: String = "Chọn địa chỉ giao hàng",
    val errorMessage: String? = null,
    // THÊM: Thuộc tính lưu số lượng tin nhắn chưa đọc
    val unreadMessageCount: Int = 0
)

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val foodRepository: CustomerFoodRepository,
    private val addressRepository: MockAddressRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val merchantRepository: MerchantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    private val _unreadCount = MutableStateFlow(0)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadRealData()
        startListeningToUnreadCount()
    }

    private fun startListeningToUnreadCount() {
        viewModelScope.launch {
            try {
                val user = authRepository.resolveStoredSession()
                val currentUserId = user?.id ?: return@launch
                chatRepository.listenToUnreadCount(currentUserId).collect { count ->
                    _unreadCount.value = count
                    _uiState.update { it.copy(unreadMessageCount = count) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        loadRealData()
    }

    private fun loadRealData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val fetchedCategories = foodRepository.getCategories()

                addressRepository.addresses.collect { addresses ->
                    val defaultAddressObj = addresses.firstOrNull { it.isDefault }
                    val defaultAddress = defaultAddressObj?.detail ?: "Chọn địa chỉ giao hàng"
                    val customerLat = defaultAddressObj?.latitude ?: 21.028511
                    val customerLng = defaultAddressObj?.longitude ?: 105.804817

                    foodRepository.getRecommendedFoods().collect { realFoods ->
                        val allStores = merchantRepository.getAllStores()
                        val storeSales = realFoods.groupBy { it.storeId }
                            .mapValues { entry -> entry.value.sumOf { it.soldCount } }

                        val featuredStores = allStores.filter { store ->
                            val sLat = store.lat
                            val sLng = store.lng
                            if (sLat != null && sLng != null) {
                                calculateDistance(customerLat, customerLng, sLat, sLng) <= 3.0
                            } else {
                                false
                            }
                        }
                        .sortedByDescending { store -> storeSales[store.id] ?: 0 }
                        .take(10)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                recommendedFoods = realFoods,
                                featuredStores = featuredStores,
                                categories = fetchedCategories,
                                address = defaultAddress,
                                errorMessage = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Không thể tải dữ liệu lúc này. Vui lòng thử lại sau."
                    )
                }
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

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
