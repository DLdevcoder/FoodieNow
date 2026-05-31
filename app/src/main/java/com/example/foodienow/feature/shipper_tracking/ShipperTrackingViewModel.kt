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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject
import kotlin.math.*

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

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val httpClient = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }
    private val goongApiKey = BuildConfig.GOONG_API_KEY

    // Biến lưu tọa độ trước đó để kiểm tra khoảng cách
    private var lastRouteUpdateLat: Double? = null
    private var lastRouteUpdateLng: Double? = null
    private val DISTANCE_THRESHOLD_METERS = 50.0 // Lấy lại route nếu di chuyển > 50m

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

    private var lastDbUpdateTimestamp: Long = 0
    private val DB_UPDATE_INTERVAL_MS = 10000L
    fun updateShipperLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastDbUpdateTimestamp > DB_UPDATE_INTERVAL_MS) {
                    lastDbUpdateTimestamp = currentTime
                    launch {
                        try { orderRepository.updateShipperLocation(orderId, lat, lng) }
                        catch (e: Exception) { Log.e("Tracking", "Lỗi update tọa độ lên DB: ${e.message}") }
                    }
                }

                _currentOrder.update { it?.copy(shipperLat = lat, shipperLng = lng) }
                val currentOrderVal = _currentOrder.value ?: return@launch
                val shouldUpdateRoute = lastRouteUpdateLat == null || lastRouteUpdateLng == null ||
                        calculateDistanceInMeters(lat, lng, lastRouteUpdateLat!!, lastRouteUpdateLng!!) > DISTANCE_THRESHOLD_METERS
                if (shouldUpdateRoute) {
                    lastRouteUpdateLat = lat
                    lastRouteUpdateLng = lng

                    if (currentOrderVal.status == OrderStatus.PICKING_UP) {
                        val storeLat = currentOrderVal.merchantLat
                        val storeLng = currentOrderVal.merchantLng
                        if (storeLat != null && storeLng != null) {
                            _routeToStore.value = getRoutePolyline(
                                origin = "$lat,$lng",
                                destination = "$storeLat,$storeLng"
                            )
                        }
                    } else if (currentOrderVal.status == OrderStatus.DELIVERING) {
                        val custLat = currentOrderVal.deliveryLat
                        val custLng = currentOrderVal.deliveryLng
                        if (custLat != null && custLng != null) {
                            _routeToCustomer.value = getRoutePolyline(
                                origin = "$lat,$lng",
                                destination = "$custLat,$custLng"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                    OrderStatus.PICKING_UP -> {
                        val route1Deferred = async { getRoutePolyline("$shipperLat,$shipperLng", "$storeLat,$storeLng") }
                        val route2Deferred = async { getRoutePolyline("$storeLat,$storeLng", "$custLat,$custLng") }

                        _routeToStore.value = route1Deferred.await()
                        _routeToCustomer.value = route2Deferred.await()
                    }
                    OrderStatus.DELIVERING -> {
                        _routeToStore.value = emptyList()
                        _routeToCustomer.value = getRoutePolyline(
                            origin = "$shipperLat,$shipperLng",
                            destination = "$custLat,$custLng"
                        )
                    }
                    else -> {
                        _routeToStore.value = emptyList()
                        _routeToCustomer.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("GoongAPI", "Lỗi tìm đường: ${e.message}")
            }
        }
    }

    fun confirmOrderPickedUp() {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // Tối ưu UI: Reset cả 2 đường hiện tại ngay lập tức để xóa trên bản đồ
                _routeToStore.value = emptyList()
                _routeToCustomer.value = emptyList()

                // Cập nhật state UI sang DELIVERING
                _currentOrder.update { it?.copy(status = OrderStatus.DELIVERING) }

                // Gọi server
                val result = orderRepository.updateOrderStatus(orderId, OrderStatus.DELIVERING)
                if (result.isSuccess) {
                    _currentOrder.value?.let { fetchRoutesBasedOnStatus(it) }
                } else {
                    // Trả lại trạng thái cũ nếu server lỗi
                    _currentOrder.update { it?.copy(status = OrderStatus.PICKING_UP) }
                    _currentOrder.value?.let { fetchRoutesBasedOnStatus(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _currentOrder.update { it?.copy(status = OrderStatus.PICKING_UP) }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun confirmDelivery(onSuccess: () -> Unit) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = orderRepository.confirmShipperDelivery(orderId)
                if (result.isSuccess) {
                    _currentOrder.update { it?.copy(shipperConfirmed = true) }
                    val latestOrder = orderRepository.getOrderById(orderId)
                    if (latestOrder?.status == OrderStatus.COMPLETED) {
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun cancelDelivery(onSuccess: () -> Unit) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = orderRepository.shipperCancelOrder(orderId)
                if (result.isSuccess) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
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

    private fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}