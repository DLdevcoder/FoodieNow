package com.example.foodienow.feature.shipper_tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.repository.OrderRepository
import com.google.android.gms.maps.model.LatLng
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
import javax.inject.Inject
import com.example.foodienow.BuildConfig
import android.util.Log

// --- Các lớp dữ liệu để Parse JSON từ Google Directions API ---
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

// Hàm giải mã chuỗi Polyline của Google thành danh sách tọa độ LatLng
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

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    private val httpClient = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }

    // Lấy API Key an toàn từ BuildConfig
    private val googleApiKey = BuildConfig.MAPS_API_KEY

    init {
        loadOrder()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            try {
                // Lấy dữ liệu thật từ Repository
                val order = orderRepository.getOrderById(orderId)
                _currentOrder.value = order

                // Vẽ tuyến đường từ vị trí Shipper (hoặc Merchant) đến điểm giao
                val startLat = order?.shipperLat ?: order?.merchantLat
                val startLng = order?.shipperLng ?: order?.merchantLng

                if (startLat != null && startLng != null &&
                    order?.deliveryLat != null && order.deliveryLng != null) {

                    fetchRoute(
                        origin = "$startLat,$startLng",
                        destination = "${order.deliveryLat},${order.deliveryLng}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateShipperLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                // Cập nhật tọa độ lên Supabase
                orderRepository.updateShipperLocation(orderId, lat, lng)

                // Cập nhật state cục bộ để UI phản hồi ngay lập tức
                _currentOrder.update {
                    it?.copy(shipperLat = lat, shipperLng = lng)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchRoute(origin: String, destination: String) {
        viewModelScope.launch {
            if (googleApiKey.isBlank()) return@launch
            Log.d("DirectionsAPI", "Key đang dùng là: $googleApiKey")
            try {
                val url = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&key=$googleApiKey"
                val response: String = httpClient.get(url).body()

                // IN KẾT QUẢ RA LOGCAT ĐỂ XEM GOOGLE TRẢ LỜI GÌ
                Log.d("DirectionsAPI", "Kết quả từ Google: $response")

                val directions = json.decodeFromString<DirectionsResponse>(response)

                if (directions.routes.isNotEmpty()) {
                    val encodedPolyline = directions.routes[0].overview_polyline.points
                    _routePoints.value = decodePolyline(encodedPolyline)
                } else {
                    Log.e("DirectionsAPI", "Danh sách tuyến đường rỗng!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}