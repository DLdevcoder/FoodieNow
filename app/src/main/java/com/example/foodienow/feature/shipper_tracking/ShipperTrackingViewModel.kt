package com.example.foodienow.feature.shipper_tracking

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.BuildConfig
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject

@Serializable
data class DirectionsResponse(
    val routes: List<RouteItem> = emptyList()
)

@Serializable
data class RouteItem(
    val overview_polyline: OverviewPolyline
)

@Serializable
data class OverviewPolyline(
    val points: String
)

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) -(result shr 1) else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) -(result shr 1) else result shr 1
        lng += dlng

        val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        poly.add(p)
    }
    return poly
}

@HiltViewModel
class ShipperTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()

    private val _routeToStore = MutableStateFlow<List<LatLng>>(emptyList())
    val routeToStore: StateFlow<List<LatLng>> = _routeToStore.asStateFlow()

    private val _routeToCustomer = MutableStateFlow<List<LatLng>>(emptyList())
    val routeToCustomer: StateFlow<List<LatLng>> = _routeToCustomer.asStateFlow()

    private val httpClient = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }

    private val goongApiKey = BuildConfig.GOONG_API_KEY

    init {
        loadOrder()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            try {
                val order = orderRepository.getOrderById(orderId)
                _currentOrder.value = order
                order?.let { fetchRoutesBasedOnStatus(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateShipperLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                // 1. Cập nhật vị trí GPS lên Supabase
                orderRepository.updateShipperLocation(orderId, lat, lng)

                // 2. Cập nhật State nội bộ
                _currentOrder.update {
                    it?.copy(shipperLat = lat, shipperLng = lng)
                }

                // 3. Nếu đang đi giao hàng, tính lại đoạn đường từ vị trí mới đến khách hàng
                val currentOrderVal = _currentOrder.value
                if (currentOrderVal?.status == OrderStatus.DELIVERING) {
                    val custLat = currentOrderVal.deliveryLat
                    val custLng = currentOrderVal.deliveryLng
                    if (custLat != null && custLng != null) {
                        _routeToCustomer.value = getRoutePolyline(
                            origin = "$lat,$lng",
                            destination = "$custLat,$custLng"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Sửa lại hàm này để logic rõ ràng hơn với DRIVER_ASSIGNED
    private fun fetchRoutesBasedOnStatus(order: Order) {
        viewModelScope.launch {
            if (goongApiKey.isBlank()) return@launch

            val shipperLat = order.shipperLat ?: return@launch
            val shipperLng = order.shipperLng ?: return@launch
            val storeLat = order.merchantLat ?: return@launch
            val storeLng = order.merchantLng ?: return@launch
            val custLat = order.deliveryLat ?: return@launch
            val custLng = order.deliveryLng ?: return@launch

            try {
                when (order.status) {
                    OrderStatus.DELIVERING -> {
                        // Đã nhận hàng: Bỏ đoạn đường đến cửa hàng, chỉ vẽ đoạn đến khách hàng
                        _routeToStore.value = emptyList()
                        _routeToCustomer.value = getRoutePolyline(
                            origin = "$shipperLat,$shipperLng",
                            destination = "$custLat,$custLng"
                        )
                    }
                    OrderStatus.DRIVER_ASSIGNED -> {
                        // Đang đi lấy hàng: Vẽ cả 2 chặng
                        _routeToStore.value = getRoutePolyline(
                            origin = "$shipperLat,$shipperLng",
                            destination = "$storeLat,$storeLng"
                        )
                        _routeToCustomer.value = getRoutePolyline(
                            origin = "$storeLat,$storeLng",
                            destination = "$custLat,$custLng"
                        )
                    }
                    else -> {
                        // Các trạng thái khác không vẽ đường
                        _routeToStore.value = emptyList()
                        _routeToCustomer.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("GoongAPI", "Lỗi tìm đường: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Thêm hàm hoàn thành đơn hàng từ màn hình Tracking
    fun completeOrder(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Cập nhật trạng thái COMPLETED lên Supabase
                val result = orderRepository.updateOrderStatus(orderId, OrderStatus.COMPLETED)
                if (result.isSuccess) {
                    _currentOrder.update { it?.copy(status = OrderStatus.COMPLETED) }
                    // Gọi callback để tự động đóng màn hình bản đồ
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun confirmOrderPickedUp() {
        viewModelScope.launch {
            try {
                // Đổi trạng thái đơn hàng trên Supabase
                orderRepository.updateOrderStatus(orderId, OrderStatus.DELIVERING)

                // Cập nhật State
                _currentOrder.update { it?.copy(status = OrderStatus.DELIVERING) }

                // Vẽ lại cấu trúc đường mới theo trạng thái DELIVERING
                _currentOrder.value?.let { fetchRoutesBasedOnStatus(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getRoutePolyline(origin: String, destination: String): List<LatLng> {
        val url = "https://rsapi.goong.io/Direction?origin=$origin&destination=$destination&vehicle=bike&api_key=$goongApiKey"
        val response: String = httpClient.get(url).body()
        val directions = json.decodeFromString<DirectionsResponse>(response)

        return if (directions.routes.isNotEmpty()) {
            decodePolyline(directions.routes[0].overview_polyline.points)
        } else {
            emptyList()
        }
    }
}