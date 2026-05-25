package com.example.foodienow.feature.shipper

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.*
import javax.inject.Inject

data class ShipperUiState(
    val isLoading: Boolean = false,
    val availableOrders: List<Order> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val completedOrders: List<Order> = emptyList(),
    val currentShipperId: String? = null,
    val error: String? = null,
    val isAutoAcceptEnabled: Boolean = false, // Trạng thái nút tự động nhận
    val shipperLat: Double? = null,           // Vị trí của Shipper
    val shipperLng: Double? = null
)

@HiltViewModel
class ShipperViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipperUiState())
    val uiState: StateFlow<ShipperUiState> = _uiState.asStateFlow()

    private val MAX_DISTANCE_KM = 3.0 // Bán kính nhận đơn

    init {
        loadShipperData()
    }

    private fun loadShipperData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUser = authRepository.getAuthState().firstOrNull()
                val shipperId = currentUser?.id

                if (shipperId != null) {
                    _uiState.update { it.copy(currentShipperId = shipperId) }

                    launch {
                        orderRepository.getAvailableDeliveries().collect { orders ->
                            processAvailableOrders(orders)
                        }
                    }

                    launch {
                        orderRepository.getShipperActiveOrder(shipperId).collect { orders ->
                            _uiState.update { state ->
                                state.copy(
                                    activeOrders = orders.sortedBy { it.createdAt },
                                    isLoading = false
                                )
                            }
                        }
                    }

                    launch {
                        orderRepository.getShipperCompletedOrders(shipperId).collect { orders ->
                            _uiState.update { state ->
                                state.copy(
                                    completedOrders = orders.sortedByDescending { it.createdAt }
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy thông tin Shipper") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(shipperLat = lat, shipperLng = lng) }
    }

    fun toggleAutoAccept(enabled: Boolean) {
        _uiState.update { it.copy(isAutoAcceptEnabled = enabled) }
    }

    private fun processAvailableOrders(allOrders: List<Order>) {
        val currentState = _uiState.value
        val lat = currentState.shipperLat
        val lng = currentState.shipperLng

        if (lat == null || lng == null) {
            _uiState.update { it.copy(availableOrders = emptyList()) }
            return
        }

        // Lọc đơn trong 3km
        val nearbyOrders = allOrders.filter { order ->
            val storeLat = order.merchantLat
            val storeLng = order.merchantLng
            if (storeLat != null && storeLng != null) {
                calculateHaversineDistance(lat, lng, storeLat, storeLng) <= MAX_DISTANCE_KM
            } else false
        }.sortedBy { it.createdAt }

        _uiState.update { it.copy(availableOrders = nearbyOrders) }

        // Logic tự động nhận đơn gần nhất nếu tài xế đang rảnh
        if (currentState.isAutoAcceptEnabled && nearbyOrders.isNotEmpty() && currentState.activeOrders.isEmpty()) {
            val bestOrder = nearbyOrders.minByOrNull { order ->
                calculateHaversineDistance(lat, lng, order.merchantLat!!, order.merchantLng!!)
            }
            bestOrder?.id?.let { acceptOrder(it) }
        }
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun acceptOrder(orderId: String) {
        val shipperId = _uiState.value.currentShipperId ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    availableOrders = state.availableOrders.filter { it.id != orderId }
                )
            }
            val result = orderRepository.acceptOrder(orderId, shipperId)

            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi nhận đơn: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Không thể nhận đơn. Vui lòng thử lại.") }
            }
        }
    }

    fun markAsDelivering(orderId: String) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, OrderStatus.DELIVERING)
            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi chuyển trạng thái đang giao: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Lỗi cập nhật trạng thái. Vui lòng thử lại.") }
            }
        }
    }

    // Đã thay đổi logic bên trong thành luồng xác nhận song song, nhưng vẫn giữ đúng TÊN HÀM completeOrder của bạn
    fun completeOrder(orderId: String) {
        viewModelScope.launch {
            val result = orderRepository.confirmShipperDelivery(orderId)
            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi hoàn tất đơn: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Không thể hoàn thành đơn. Vui lòng thử lại.") }
            }
        }
    }
}