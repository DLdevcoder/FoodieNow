package com.example.foodienow.feature.shipper_tracking

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.BuildConfig
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.repository.OrderRepository
import org.maplibre.android.geometry.LatLng
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

    // SỬA ĐỔI: Tách thành 2 list tọa độ độc lập
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

                val storeLat = order?.merchantLat
                val storeLng = order?.merchantLng
                val custLat = order?.deliveryLat
                val custLng = order?.deliveryLng
                val shipperLat = order?.shipperLat
                val shipperLng = order?.shipperLng

                if (storeLat != null && storeLng != null && custLat != null && custLng != null) {
                    fetchFullRoute(shipperLat, shipperLng, storeLat, storeLng, custLat, custLng)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateShipperLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                orderRepository.updateShipperLocation(orderId, lat, lng)
                _currentOrder.update {
                    it?.copy(shipperLat = lat, shipperLng = lng)
                }
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

    private fun fetchFullRoute(
        shipperLat: Double?, shipperLng: Double?,
        storeLat: Double, storeLng: Double,
        custLat: Double, custLng: Double
    ) {
        viewModelScope.launch {
            if (goongApiKey.isBlank()) return@launch

            try {
                // Chặng 1: Từ Shipper đến Cửa hàng
                if (shipperLat != null && shipperLng != null) {
                    _routeToStore.value = getRoutePolyline("$shipperLat,$shipperLng", "$storeLat,$storeLng")
                }

                // Chặng 2: Từ Cửa hàng đến Khách hàng
                _routeToCustomer.value = getRoutePolyline("$storeLat,$storeLng", "$custLat,$custLng")

            } catch (e: Exception) {
                Log.e("GoongAPI", "Lỗi tìm đường: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}