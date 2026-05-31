package com.example.foodienow.feature.shipper

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    val shipperLng: Double? = null,
    val processingOrderIds: Set<String> = emptySet()
)

@HiltViewModel
class ShipperViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipperUiState())
    val uiState: StateFlow<ShipperUiState> = _uiState.asStateFlow()

    private var rawPendingOrders: List<Order> = emptyList()

    // ĐỔI TÊN BIẾN NÀY THÀNH _handledOrderIds (chứa cả đơn đã hủy và đơn ĐÃ NHẬN THÀNH CÔNG)
    private val _handledOrderIds = MutableStateFlow<Set<String>>(emptySet())

    private val MAX_DISTANCE_KM = 3.0
    private var availableJob: Job? = null
    private var activeJob: Job? = null
    private var completedJob: Job? = null

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

                    availableJob?.cancel()
                    activeJob?.cancel()
                    completedJob?.cancel()

                    availableJob = launch {
                        orderRepository.getAvailableDeliveries().collect { orders ->
                            rawPendingOrders = orders
                            processAvailableOrders(rawPendingOrders)
                        }
                    }

                    activeJob = launch {
                        orderRepository.getShipperActiveOrder(shipperId).collect { orders ->
                            _uiState.update { state ->
                                state.copy(
                                    activeOrders = orders.sortedBy { it.createdAt },
                                    isLoading = false
                                )
                            }
                        }
                    }

                    completedJob = launch {
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
        processAvailableOrders(rawPendingOrders)
    }

    private fun processAvailableOrders(allOrders: List<Order>) {
        val currentState = _uiState.value
        val lat = currentState.shipperLat
        val lng = currentState.shipperLng

        // Lấy danh sách đen ra kiểm tra
        val handledSet = _handledOrderIds.value

        if (lat == null || lng == null) {
            _uiState.update { it.copy(availableOrders = emptyList()) }
            return
        }

        val nearbyOrders = allOrders.filter { order ->
            val orderId = order.id
            val storeLat = order.merchantLat
            val storeLng = order.merchantLng

            // NẾU ĐƠN HÀNG NẰM TRONG DANH SÁCH ĐEN -> BỎ QUA LUÔN, KHÔNG HIỂN THỊ LẠI
            if (orderId != null && handledSet.contains(orderId)) {
                false
            } else if (storeLat != null && storeLng != null) {
                val distance = calculateHaversineDistance(lat, lng, storeLat, storeLng)
                distance <= MAX_DISTANCE_KM
            } else {
                false
            }
        }.sortedBy { it.createdAt }

        _uiState.update { it.copy(availableOrders = nearbyOrders) }

        if (currentState.isAutoAcceptEnabled && nearbyOrders.isNotEmpty() && currentState.activeOrders.isEmpty()) {
            val bestOrder = nearbyOrders.minByOrNull { order ->
                calculateHaversineDistance(lat, lng, order.merchantLat!!, order.merchantLng!!)
            }
            if (bestOrder?.id != null && !currentState.processingOrderIds.contains(bestOrder.id)) {
                acceptOrder(bestOrder.id)
            }
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
        val currentState = _uiState.value
        if (currentState.processingOrderIds.contains(orderId)) return

        val shipperId = currentState.currentShipperId ?: return

        // KIỂM TRA: Phải có tọa độ mới cho nhận đơn
        val lat = currentState.shipperLat
        val lng = currentState.shipperLng
        if (lat == null || lng == null) {
            _uiState.update { it.copy(error = "Không thể nhận đơn: Chưa lấy được vị trí GPS của bạn.") }
            return
        }

        viewModelScope.launch {
            val orderToMove = currentState.availableOrders.find { it.id == orderId }
            _uiState.update { state ->
                state.copy(
                    availableOrders = state.availableOrders.filter { it.id != orderId },
                    processingOrderIds = state.processingOrderIds + orderId
                )
            }

            // TRUYỀN lat VÀ lng VÀO ĐÂY
            val result = orderRepository.shipperAcceptOrder(orderId, shipperId, lat, lng)

            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi nhận đơn: ", result.exceptionOrNull())
                _uiState.update { state ->
                    val restoredOrders = if (orderToMove != null && !state.availableOrders.contains(orderToMove)) {
                        state.availableOrders + orderToMove
                    } else state.availableOrders

                    state.copy(
                        error = "Không thể nhận đơn. Đơn có thể đã bị hủy hoặc được shipper khác nhận.",
                        availableOrders = restoredOrders,
                        processingOrderIds = state.processingOrderIds - orderId
                    )
                }
            } else {
                _handledOrderIds.update { it + orderId }

                _uiState.update { state ->
                    state.copy(processingOrderIds = state.processingOrderIds - orderId)
                }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _handledOrderIds.update { it + orderId }

            val result = orderRepository.shipperCancelOrder(orderId)
            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi hủy đơn: ", result.exceptionOrNull())
                _handledOrderIds.update { it - orderId }
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

    fun markOrderAsPickedUp(orderId: String) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, OrderStatus.DELIVERING)
            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi cập nhật lấy hàng: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Không thể cập nhật. Vui lòng thử lại.") }
            }
        }
    }
}