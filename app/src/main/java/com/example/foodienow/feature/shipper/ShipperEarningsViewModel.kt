package com.example.foodienow.feature.shipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ShipperEarningsUiState(
    val isLoading: Boolean = false,
    val todayEarnings: Double = 0.0,
    val weekEarnings: Double = 0.0,
    val monthEarnings: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class ShipperEarningsViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipperEarningsUiState())
    val uiState: StateFlow<ShipperEarningsUiState> = _uiState.asStateFlow()

    init {
        loadEarnings()
    }

    private fun loadEarnings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Trong thực tế, bạn sẽ cần API để lấy dữ liệu thu nhập
                // Hiện tại sử dụng mock data
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        todayEarnings = 150000.0,
                        weekEarnings = 850000.0,
                        monthEarnings = 3200000.0
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}