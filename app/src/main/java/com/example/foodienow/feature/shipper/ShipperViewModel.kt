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
            // 1. Lấy đơn hàng ra để chuẩn bị di chuyển
            val orderToMove = currentState.availableOrders.find { it.id == orderId }

            // 2. OPTIMISTIC UPDATE: Cập nhật giao diện siêu tốc
            _uiState.update { state ->
                // Xóa khỏi tab Đơn sẵn sàng
                val newAvailableOrders = state.availableOrders.filter { it.id != orderId }

                // Bơm thẳng sang tab Đang giao với trạng thái PICKING_UP
                val newActiveOrders = if (orderToMove != null) {
                    val updatedOrder = orderToMove.copy(
                        status = OrderStatus.PICKING_UP,
                        shipperId = shipperId
                    )
                    listOf(updatedOrder) + state.activeOrders
                } else {
                    state.activeOrders
                }

                state.copy(
                    availableOrders = newAvailableOrders,
                    activeOrders = newActiveOrders.sortedBy { it.createdAt }, // Hiện ngay lập tức
                    processingOrderIds = state.processingOrderIds + orderId
                )
            }

            // 3. Gửi lệnh lên Server chạy ngầm
            val result = orderRepository.shipperAcceptOrder(orderId, shipperId, lat, lng)

            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi nhận đơn: ", result.exceptionOrNull())
                // PHỤC HỒI LẠI TRẠNG THÁI CŨ NẾU LỖI
                _uiState.update { state ->
                    val restoredAvailable = if (orderToMove != null && !state.availableOrders.contains(orderToMove)) {
                        state.availableOrders + orderToMove
                    } else state.availableOrders

                    val restoredActive = state.activeOrders.filter { it.id != orderId }

                    state.copy(
                        error = "Không thể nhận đơn. Đơn có thể đã bị hủy hoặc được shipper khác nhận.",
                        availableOrders = restoredAvailable,
                        activeOrders = restoredActive,
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

    fun markOrderAsPickedUp(orderId: String) {
        val currentState = _uiState.value
        if (currentState.processingOrderIds.contains(orderId)) return

        viewModelScope.launch {
            // 1. OPTIMISTIC UPDATE: Đổi chữ "Đã lấy hàng" thành "Đã giao" ngay lập tức
            _uiState.update { state ->
                val updatedActiveOrders = state.activeOrders.map {
                    if (it.id == orderId) it.copy(status = OrderStatus.DELIVERING) else it
                }
                state.copy(
                    activeOrders = updatedActiveOrders,
                    processingOrderIds = state.processingOrderIds + orderId
                )
            }

            // 2. Cập nhật server
            val result = orderRepository.updateOrderStatus(orderId, OrderStatus.DELIVERING)

            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi cập nhật lấy hàng: ", result.exceptionOrNull())
                // Phục hồi nếu lỗi
                _uiState.update { state ->
                    val restoredOrders = state.activeOrders.map {
                        if (it.id == orderId) it.copy(status = OrderStatus.PICKING_UP) else it
                    }
                    state.copy(
                        error = "Không thể cập nhật. Vui lòng thử lại.",
                        activeOrders = restoredOrders,
                        processingOrderIds = state.processingOrderIds - orderId
                    )
                }
            } else {
                _uiState.update { it.copy(processingOrderIds = it.processingOrderIds - orderId) }
            }
        }
    }

    fun completeOrder(orderId: String) {
        val currentState = _uiState.value
        if (currentState.processingOrderIds.contains(orderId)) return

        viewModelScope.launch {
            _uiState.update { it.copy(processingOrderIds = it.processingOrderIds + orderId) }

            val result = orderRepository.confirmShipperDelivery(orderId)

            if (result.isFailure) {
                Log.e("ShipperApp", "Lỗi hoàn tất đơn: ", result.exceptionOrNull())
                _uiState.update { it.copy(error = "Không thể hoàn thành đơn. Vui lòng thử lại.") }
            } else {
                // Tối ưu UI: Vô hiệu hóa nút và đổi sang "Chờ khách xác nhận" ngay lập tức
                _uiState.update { state ->
                    val updatedActiveOrders = state.activeOrders.map {
                        if (it.id == orderId) it.copy(shipperConfirmed = true) else it
                    }
                    state.copy(activeOrders = updatedActiveOrders)
                }
            }

            _uiState.update { it.copy(processingOrderIds = it.processingOrderIds - orderId) }
        }
    }
}