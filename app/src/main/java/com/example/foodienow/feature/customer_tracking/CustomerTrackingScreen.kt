package com.example.foodienow.feature.customer_tracking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.foodienow.BuildConfig
import com.example.foodienow.R
import com.example.foodienow.domain.model.OrderStatus
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import android.graphics.Color as AndroidColor

fun createMapIcon(context: Context, drawableId: Int, dpSize: Int): Icon? {
    return try {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        val px = (dpSize * context.resources.displayMetrics.density).toInt()
        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        IconFactory.getInstance(context).fromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTrackingScreen(
    onBack: () -> Unit,
    viewModel: CustomerTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val order by viewModel.currentOrder.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()

    val mapView = remember { MapLibre.getInstance(context); MapView(context) }
    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var isCameraInitialized by remember { mutableStateOf(false) }

    var activePolyline by remember { mutableStateOf<Polyline?>(null) }
    var storeMarker by remember { mutableStateOf<Marker?>(null) }
    var customerMarker by remember { mutableStateOf<Marker?>(null) }
    var shipperMarker by remember { mutableStateOf<Marker?>(null) }

    val storeIcon = remember(context) { createMapIcon(context, R.drawable.ic_store, 40) }
    val customerIcon = remember(context) { createMapIcon(context, R.drawable.ic_customer, 40) }
    val shipperIcon = remember(context) { createMapIcon(context, R.drawable.ic_shipper, 45) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Effect này chạy để vẽ Đường đi, Quán ăn và Khách hàng
    LaunchedEffect(isMapReady, routePoints, order?.status, mapLibreMapInstance) {
        if (!isMapReady) return@LaunchedEffect
        val map = mapLibreMapInstance ?: return@LaunchedEffect
        val currentOrder = order ?: return@LaunchedEffect

        // Vẽ đường khi đang trong 2 trạng thái cần thiết
        if (currentOrder.status == OrderStatus.DRIVER_ASSIGNED || currentOrder.status == OrderStatus.DELIVERING) {

            // 1. Cập nhật đường đi
            activePolyline?.let { map.removePolyline(it) }
            if (routePoints.isNotEmpty()) {
                activePolyline = map.addPolyline(
                    PolylineOptions().addAll(routePoints).color(AndroidColor.parseColor("#EE4D2D")).width(6f)
                )
            }

            // 2. Vẽ ghim Quán ăn
            if (storeMarker == null && currentOrder.merchantLat != null && currentOrder.merchantLng != null) {
                val opts = MarkerOptions().position(LatLng(currentOrder.merchantLat!!, currentOrder.merchantLng!!)).title("Cửa hàng")
                storeIcon?.let { opts.icon(it) }
                storeMarker = map.addMarker(opts)
            }

            // 3. Vẽ ghim Khách hàng
            if (customerMarker == null && currentOrder.deliveryLat != null && currentOrder.deliveryLng != null) {
                val opts = MarkerOptions().position(LatLng(currentOrder.deliveryLat!!, currentOrder.deliveryLng!!)).title("Vị trí của bạn")
                customerIcon?.let { opts.icon(it) }
                customerMarker = map.addMarker(opts)
            }

            // 4. Focus camera vào giữa tuyến đường khi load lần đầu
            if (!isCameraInitialized && currentOrder.merchantLat != null && currentOrder.deliveryLat != null) {
                val midLat = (currentOrder.merchantLat!! + currentOrder.deliveryLat!!) / 2
                val midLng = (currentOrder.merchantLng!! + currentOrder.deliveryLng!!) / 2
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(midLat, midLng), 13.0))
                isCameraInitialized = true
            }
        }
    }

    // Effect này xử lý hiển thị Tài xế (Chỉ hiện khi DELIVERING)
    LaunchedEffect(isMapReady, order?.shipperLat, order?.shipperLng, order?.status) {
        if (!isMapReady) return@LaunchedEffect
        val map = mapLibreMapInstance ?: return@LaunchedEffect
        val lat = order?.shipperLat ?: return@LaunchedEffect
        val lng = order?.shipperLng ?: return@LaunchedEffect
        val currentOrder = order ?: return@LaunchedEffect

        if (currentOrder.status == OrderStatus.DELIVERING) {
            val currentPos = LatLng(lat, lng)

            if (shipperMarker != null) {
                shipperMarker?.position = currentPos
            } else {
                val opts = MarkerOptions().position(currentPos).title("Tài xế")
                shipperIcon?.let { opts.icon(it) }
                shipperMarker = map.addMarker(opts)
            }
        } else {
            // Nếu trạng thái quay về (hoặc vẫn đang là) DRIVER_ASSIGNED, ẩn/xóa marker của tài xế
            shipperMarker?.let { map.removeMarker(it) }
            shipperMarker = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theo dõi đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại") }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val currentOrder = order

            if (currentOrder == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // Luôn render bản đồ
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        view.getMapAsync { map ->
                            if (mapLibreMapInstance == null) {
                                mapLibreMapInstance = map
                                map.setStyle("https://tiles.goong.io/assets/goong_map_web.json?api_key=${BuildConfig.GOONG_MAPTILES_KEY}") {
                                    isMapReady = true
                                }
                            }
                        }
                    }
                )

                // Khối thông tin phía dưới cùng
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val statusText = when (currentOrder.status) {
                            OrderStatus.DRIVER_ASSIGNED -> "Tài xế đang đến nhà hàng lấy thức ăn"
                            OrderStatus.DELIVERING -> "Tài xế đang trên đường giao đến bạn"
                            else -> "Đơn hàng đang xử lý"
                        }

                        Text(
                            text = statusText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Mã đơn: #${currentOrder.id?.take(8)}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Địa chỉ nhận: ${currentOrder.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}