package com.example.foodienow.feature.shipper_tracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.location.*
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
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import android.graphics.Color as AndroidColor

fun createMapIcon(context: Context, drawableId: Int, dpSize: Int): Icon? {
    return try {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        val px = (dpSize * context.resources.displayMetrics.density).toInt()
        // Bắt buộc dùng ARGB_8888 để MapLibre không bị lỗi trong suốt
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
fun ShipperTrackingScreen(
    onBack: () -> Unit,
    viewModel: ShipperTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val order by viewModel.currentOrder.collectAsState()
    val routeToStore by viewModel.routeToStore.collectAsState()
    val routeToCustomer by viewModel.routeToCustomer.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapLibre.getInstance(context); MapView(context) }

    // TRẠNG THÁI QUẢN LÝ BẢN ĐỒ
    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var isMapReady by remember { mutableStateOf(false) } // CHÌA KHÓA: Đợi Style load xong mới vẽ
    var isCameraInitialized by remember { mutableStateOf(false) }

    // THAM CHIẾU CÁC ĐỐI TƯỢNG (Để xóa/sửa riêng lẻ, không dùng map.clear)
    var polylineToStore by remember { mutableStateOf<Polyline?>(null) }
    var polylineToCustomer by remember { mutableStateOf<Polyline?>(null) }
    var storeMarker by remember { mutableStateOf<Marker?>(null) }
    var customerMarker by remember { mutableStateOf<Marker?>(null) }
    var shipperMarker by remember { mutableStateOf<Marker?>(null) }

    // Load Icons
    val merchantIcon = remember(context) { createMapIcon(context, R.drawable.ic_store, 40) }
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

    DisposableEffect(hasLocationPermission) {
        var locationCallback: LocationCallback? = null
        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        viewModel.updateShipperLocation(location.latitude, location.longitude)
                    }
                }
            }
            try { fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()) }
            catch (e: Exception) { e.printStackTrace() }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        onDispose { locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) } }
    }

    // KHỐI VẼ ĐƯỜNG VÀ ĐIỂM CỐ ĐỊNH (Chỉ chạy khi map đã Ready)
    LaunchedEffect(isMapReady, routeToStore, routeToCustomer, order?.merchantLat, order?.deliveryLat) {
        if (!isMapReady) return@LaunchedEffect
        val map = mapLibreMapInstance ?: return@LaunchedEffect

        // Xử lý Route 1 (Xanh - Đến Store)
        polylineToStore?.let { map.removePolyline(it) } // Xóa đường cũ nếu có
        if (routeToStore.isNotEmpty()) {
            polylineToStore = map.addPolyline(PolylineOptions().addAll(routeToStore).color(AndroidColor.parseColor("#0088FF")).width(6f))
        }

        // Xử lý Route 2 (Cam - Đến Khách)
        polylineToCustomer?.let { map.removePolyline(it) }
        if (routeToCustomer.isNotEmpty()) {
            polylineToCustomer = map.addPolyline(PolylineOptions().addAll(routeToCustomer).color(AndroidColor.parseColor("#EE4D2D")).width(6f))
        }

        // Vẽ Store Marker (Chỉ vẽ 1 lần)
        if (storeMarker == null && order?.merchantLat != null && order?.merchantLng != null) {
            val opts = MarkerOptions().position(LatLng(order!!.merchantLat!!, order!!.merchantLng!!)).title("Cửa hàng")
            merchantIcon?.let { opts.icon(it) }
            storeMarker = map.addMarker(opts)
        }

        // Vẽ Customer Marker (Chỉ vẽ 1 lần)
        if (customerMarker == null && order?.deliveryLat != null && order?.deliveryLng != null) {
            val opts = MarkerOptions().position(LatLng(order!!.deliveryLat!!, order!!.deliveryLng!!)).title("Khách hàng")
            customerIcon?.let { opts.icon(it) }
            customerMarker = map.addMarker(opts)
        }
    }

    // KHỐI VẼ SHIPPER ĐỘNG (Chạy khi map Ready và có tọa độ mới)
    LaunchedEffect(isMapReady, order?.shipperLat, order?.shipperLng) {
        if (!isMapReady) return@LaunchedEffect
        val map = mapLibreMapInstance ?: return@LaunchedEffect
        val lat = order?.shipperLat ?: return@LaunchedEffect
        val lng = order?.shipperLng ?: return@LaunchedEffect
        val currentPos = LatLng(lat, lng)

        if (shipperMarker != null) {
            // Đã có icon -> Trượt nó đi
            shipperMarker?.position = currentPos
        } else {
            // Chưa có -> Tạo mới
            val opts = MarkerOptions().position(currentPos).title("Tài xế")
            shipperIcon?.let { opts.icon(it) }
            shipperMarker = map.addMarker(opts)
        }

        if (!isCameraInitialized) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 15.0))
            isCameraInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đang giao hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (!hasLocationPermission) {
                Text("Vui lòng cấp quyền vị trí", modifier = Modifier.align(Alignment.Center))
            } else {
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

                order?.let { currentOrder ->
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Mã đơn: #${currentOrder.id?.take(8)}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Giao đến: ${currentOrder.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    when (currentOrder.status) {
                                        OrderStatus.DELIVERING -> viewModel.completeOrder(onSuccess = onBack)
                                        OrderStatus.DRIVER_ASSIGNED -> viewModel.confirmOrderPickedUp()
                                        else -> {}
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (currentOrder.status == OrderStatus.DELIVERING) "Hoàn thành chuyến đi" else "Đã lấy hàng")
                            }
                        }
                    }
                }
            }
        }
    }
}