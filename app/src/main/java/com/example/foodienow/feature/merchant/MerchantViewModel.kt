package com.example.foodienow.feature.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.model.Voucher
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.MerchantRepository
import com.example.foodienow.domain.repository.ChatRepository
import com.example.foodienow.domain.repository.VoucherRepository
import com.example.foodienow.data.remote.GoongAddressService
import com.example.foodienow.data.remote.GoongPrediction
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
    val unreadMessageCount: Int = 0,
    val vouchers: List<Voucher> = emptyList(),
    val isVouchersLoading: Boolean = false
)

@HiltViewModel
class MerchantViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val merchantRepository: MerchantRepository,
    private val chatRepository: ChatRepository,
    private val goongAddressService: GoongAddressService,
    private val voucherRepository: VoucherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MerchantUiState())
    val uiState: StateFlow<MerchantUiState> = _uiState.asStateFlow()

    // BƯỚC 1: Thêm luồng độc lập quản lý số đếm thông báo
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _predictions = MutableStateFlow<List<GoongPrediction>>(emptyList())
    val predictions: StateFlow<List<GoongPrediction>> = _predictions.asStateFlow()

    private val _selectedLat = MutableStateFlow<Double?>(null)
    val selectedLat: StateFlow<Double?> = _selectedLat.asStateFlow()

    private val _selectedLng = MutableStateFlow<Double?>(null)
    val selectedLng: StateFlow<Double?> = _selectedLng.asStateFlow()

    private val _selectedDetail = MutableStateFlow("")
    val selectedDetail: StateFlow<String> = _selectedDetail.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    init {
        loadMerchantData()
        // BƯỚC 2: Gọi hàm lắng nghe Real-time
        startListeningToUnreadCount()
    }

    // BƯỚC 3: Thay thế loadUnreadMessageCount bằng hàm lắng nghe liên tục
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

    fun searchAddress(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _predictions.value = emptyList()
            } else {
                _predictions.value = goongAddressService.getAutocomplete(query)
            }
        }
    }

    fun selectPrediction(prediction: GoongPrediction) {
        _predictions.value = emptyList()
        _selectedDetail.value = prediction.description
        _isResolving.value = true
        viewModelScope.launch {
            val result = goongAddressService.getPlaceDetail(prediction.placeId)
            if (result != null) {
                _selectedLat.value = result.geometry.location.lat
                _selectedLng.value = result.geometry.location.lng
                _selectedDetail.value = result.formattedAddress ?: result.name ?: prediction.description
            }
            _isResolving.value = false
        }
    }

    fun updateLocation(lat: Double, lng: Double, detail: String? = null) {
        _selectedLat.value = lat
        _selectedLng.value = lng
        if (detail != null) {
            _selectedDetail.value = detail
        } else {
            viewModelScope.launch {
                val result = goongAddressService.getReverseGeocode(lat, lng)
                if (result != null) {
                    _selectedDetail.value = result.formattedAddress ?: result.name ?: ""
                }
            }
        }
    }

    fun clearAddForm() {
        _predictions.value = emptyList()
        _selectedLat.value = null
        _selectedLng.value = null
        _selectedDetail.value = ""
        _isResolving.value = false
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
                        loadVouchers(currentUser.id)
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

    private fun loadVouchers(merchantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVouchersLoading = true) }
            val result = voucherRepository.getVouchersByMerchant(merchantId)
            result.onSuccess { voucherList ->
                _uiState.update { it.copy(vouchers = voucherList, isVouchersLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isVouchersLoading = false, error = e.message) }
            }
        }
    }

    fun createVoucher(
        code: String,
        discountPercent: Int,
        discountAmount: Long,
        minOrderValue: Long,
        maxDiscount: Long,
        isActive: Boolean,
        expiresAt: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVouchersLoading = true) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()
                if (currentUser != null) {
                    val voucher = Voucher(
                        id = "",
                        merchantId = currentUser.id,
                        code = code.trim().uppercase(),
                        discountPercent = discountPercent,
                        discountAmount = discountAmount,
                        minOrderValue = minOrderValue,
                        maxDiscount = maxDiscount,
                        isActive = isActive,
                        expiresAt = expiresAt
                    )
                    val result = voucherRepository.createVoucher(voucher)
                    result.onSuccess {
                        loadVouchers(currentUser.id)
                    }.onFailure { e ->
                        _uiState.update { it.copy(isVouchersLoading = false, error = e.message) }
                    }
                } else {
                    _uiState.update { it.copy(isVouchersLoading = false, error = "Chưa đăng nhập") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isVouchersLoading = false, error = e.message) }
            }
        }
    }

    fun updateVoucher(
        id: String,
        code: String,
        discountPercent: Int,
        discountAmount: Long,
        minOrderValue: Long,
        maxDiscount: Long,
        isActive: Boolean,
        expiresAt: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVouchersLoading = true) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()
                if (currentUser != null) {
                    val voucher = Voucher(
                        id = id,
                        merchantId = currentUser.id,
                        code = code.trim().uppercase(),
                        discountPercent = discountPercent,
                        discountAmount = discountAmount,
                        minOrderValue = minOrderValue,
                        maxDiscount = maxDiscount,
                        isActive = isActive,
                        expiresAt = expiresAt
                    )
                    val result = voucherRepository.updateVoucher(voucher)
                    result.onSuccess {
                        loadVouchers(currentUser.id)
                    }.onFailure { e ->
                        _uiState.update { it.copy(isVouchersLoading = false, error = e.message) }
                    }
                } else {
                    _uiState.update { it.copy(isVouchersLoading = false, error = "Chưa đăng nhập") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isVouchersLoading = false, error = e.message) }
            }
        }
    }

    fun deleteVoucher(voucherId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVouchersLoading = true) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()
                if (currentUser != null) {
                    val result = voucherRepository.deleteVoucher(voucherId)
                    result.onSuccess {
                        loadVouchers(currentUser.id)
                    }.onFailure { e ->
                        _uiState.update { it.copy(isVouchersLoading = false, error = e.message) }
                    }
                } else {
                    _uiState.update { it.copy(isVouchersLoading = false, error = "Chưa đăng nhập") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isVouchersLoading = false, error = e.message) }
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
}