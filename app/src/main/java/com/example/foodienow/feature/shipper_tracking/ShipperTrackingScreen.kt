package com.example.foodienow.feature.shipper_tracking

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.android.gms.location.*
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import android.graphics.Color as AndroidColor

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
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }
    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    // ---------------------------------------------------------
    // KHẮC PHỤC OOM: Cache Icon bằng remember (Khởi tạo 1 lần)
    // ---------------------------------------------------------
    val merchantIcon = remember(context) {
        val density = context.resources.displayMetrics.density
        val iconSizePx = (40 * density).toInt()
        val iconFactory = IconFactory.getInstance(context)
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_store)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, iconSizePx, iconSizePx, false)
        iconFactory.fromBitmap(scaledBitmap)
    }

    val customerIcon = remember(context) {
        val density = context.resources.displayMetrics.density
        val iconSizePx = (40 * density).toInt()
        val iconFactory = IconFactory.getInstance(context)
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_customer)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, iconSizePx, iconSizePx, false)
        iconFactory.fromBitmap(scaledBitmap)
    }
    // ---------------------------------------------------------

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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(hasLocationPermission) {
        var locationCallback: LocationCallback? = null

        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        viewModel.updateShipperLocation(location.latitude, location.longitude)

                        mapLibreMapInstance?.let { map ->
                            val currentTarget = map.cameraPosition.target
                            // Chỉ tự động di chuyển camera đến shipper ở lần đầu tiên để tránh gây khó chịu khi người dùng đang thao tác kéo map
                            if (currentTarget == null || currentTarget.latitude == 0.0) {
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(location.latitude, location.longitude), 15.0
                                    )
                                )
                            }
                        }
                    }
                }
            }
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        onDispose { locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) } }
    }

    LaunchedEffect(routeToStore, routeToCustomer, order, mapLibreMapInstance) {
        val map = mapLibreMapInstance ?: return@LaunchedEffect

        map.clear()

        if (routeToStore.isNotEmpty()) {
            map.addPolyline(
                PolylineOptions()
                    .addAll(routeToStore)
                    .color(AndroidColor.parseColor("#0088FF")) // Xanh dương cho chặng đi lấy đồ
                    .width(5f)
            )
        }

        if (routeToCustomer.isNotEmpty()) {
            map.addPolyline(
                PolylineOptions()
                    .addAll(routeToCustomer)
                    .color(AndroidColor.parseColor("#EE4D2D")) // Cam cho chặng đi giao đồ
                    .width(5f)
            )
        }

        if (order?.merchantLat != null && order?.merchantLng != null) {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(order!!.merchantLat!!, order!!.merchantLng!!))
                    .title("Cửa hàng")
                    .icon(merchantIcon)
            )
        }

        if (order?.deliveryLat != null && order?.deliveryLng != null) {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(order!!.deliveryLat!!, order!!.deliveryLng!!))
                    .title("Khách hàng")
                    .icon(customerIcon)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đang giao hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
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
                                map.setStyle("https://tiles.goong.io/assets/goong_map_web.json?api_key=${BuildConfig.GOONG_MAPTILES_KEY}")
                            }
                        }
                    }
                )

                order?.let { currentOrder ->
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
                            Text(text = "Mã đơn: #${currentOrder.id?.take(8)}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Giao đến: ${currentOrder.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { /* Xử lý hoàn thành đơn */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Hoàn thành chuyến đi")
                            }
                        }
                    }
                }
            }
        }
    }
}