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
    val isAutoAcceptEnabled: Boolean = false,
    val shipperLat: Double? = null,
    val shipperLng: Double? = null
)

@HiltViewModel
class ShipperViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipperUiState())
    val uiState: StateFlow<ShipperUiState> = _uiState.asStateFlow()
    private var rawPendingOrders: List<Order> = emptyList()
    private val _cancelledOrderIds = MutableStateFlow<Set<String>>(emptySet())

    private val MAX_DISTANCE_KM = 3.0

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
                            rawPendingOrders = orders
                            processAvailableOrders(rawPendingOrders)
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
        processAvailableOrders(rawPendingOrders)
    }

    fun toggleAutoAccept(enabled: Boolean) {
        _uiState.update { it.copy(isAutoAcceptEnabled = enabled) }
    }

    private fun processAvailableOrders(allOrders: List<Order>) {
        val currentState = _uiState.value
        val lat = currentState.shipperLat
        val lng = currentState.shipperLng
        val cancelledSet = _cancelledOrderIds.value

        Log.d("ShipperApp", "1. Tổng số đơn PREPARING từ Database: ${allOrders.size}")
        Log.d("ShipperApp", "2. Vị trí Shipper hiện tại: Lat=$lat, Lng=$lng")

        if (lat == null || lng == null) {
            Log.d("ShipperApp", "-> DỪNG: Chưa lấy được GPS của Shipper")
            _uiState.update { it.copy(availableOrders = emptyList()) }
            return
        }

        val nearbyOrders = allOrders.filter { order ->
            val orderId = order.id
            val storeLat = order.merchantLat
            val storeLng = order.merchantLng

            if (orderId != null && cancelledSet.contains(orderId)) {
                Log.d("ShipperApp", "-> LOẠI ĐƠN $orderId: Nằm trong danh sách đã hủy")
                false
            } else if (storeLat != null && storeLng != null) {
                val distance = calculateHaversineDistance(lat, lng, storeLat, storeLng)
                Log.d("ShipperApp", "-> Khoảng cách đến đơn $orderId: $distance km")

                // Trả về true nếu <= 3km
                distance <= MAX_DISTANCE_KM
            } else {
                Log.d("ShipperApp", "-> LOẠI ĐƠN $orderId: Tọa độ quán (merchant_lat/lng) bị NULL")
                false
            }
        }.sortedBy { it.createdAt }

        _uiState.update { it.copy(availableOrders = nearbyOrders) }

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
            val result = orderRepository.shipperAcceptOrder(orderId, shipperId)

            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi nhận đơn: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Không thể nhận đơn. Vui lòng thử lại.") }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _cancelledOrderIds.update { it + orderId }

            _uiState.update { state ->
                state.copy(activeOrders = state.activeOrders.filter { it.id != orderId })
            }

            val result = orderRepository.shipperCancelOrder(orderId)
            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi hủy đơn: ", result.exceptionOrNull())
                _cancelledOrderIds.update { it - orderId }
                _uiState.update { it.copy(error = "Lỗi hủy đơn. Vui lòng thử lại.") }
            }
        }
    }

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