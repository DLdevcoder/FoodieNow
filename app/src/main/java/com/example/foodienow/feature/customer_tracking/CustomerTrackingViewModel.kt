package com.example.foodienow.feature.customer_tracking

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.BuildConfig
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
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

@HiltViewModel
class CustomerTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    private val httpClient = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }
    private val goongApiKey = BuildConfig.GOONG_API_KEY

    init {
        loadInitialOrder()
        observeOrderRealtime()
    }

    private fun loadInitialOrder() {
        viewModelScope.launch {
            try {
                val order = orderRepository.getOrderById(orderId)
                _currentOrder.value = order
                order?.let { calculateRoute(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeOrderRealtime() {
        viewModelScope.launch {
            callbackFlow<Order> {
                val channel = supabaseClient.channel("customer_track_$orderId")
                val changeFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                    table = "orders"
                    filter = "id=eq.$orderId"
                }

                launch {
                    changeFlow.collect { action ->
                        try {
                            val updatedOrder = action.decodeRecord<Order>()
                            trySend(updatedOrder)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                channel.subscribe()
                awaitClose {
                    launch { supabaseClient.realtime.removeChannel(channel) }
                }
            }.collect { updatedOrder ->
                _currentOrder.value = updatedOrder
                if (_routePoints.value.isEmpty()) {
                    calculateRoute(updatedOrder)
                }
            }
        }
    }

    private suspend fun calculateRoute(order: Order) {
        if (goongApiKey.isBlank()) return

        // Vẽ tuyến đường từ CỬA HÀNG đến KHÁCH HÀNG trong cả 2 trạng thái
        if (order.status != OrderStatus.DRIVER_ASSIGNED && order.status != OrderStatus.DELIVERING) {
            _routePoints.value = emptyList()
            return
        }

        val storeLat = order.merchantLat ?: return
        val storeLng = order.merchantLng ?: return
        val custLat = order.deliveryLat ?: return
        val custLng = order.deliveryLng ?: return

        try {
            val points = getRoutePolyline(
                origin = "$storeLat,$storeLng",
                destination = "$custLat,$custLng"
            )
            _routePoints.value = points
        } catch (e: Exception) {
            Log.e("GoongAPI", "Lỗi tính toán đường đi: ${e.message}")
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

    private fun decodePolyline(encoded: String): List<LatLng> {
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
}