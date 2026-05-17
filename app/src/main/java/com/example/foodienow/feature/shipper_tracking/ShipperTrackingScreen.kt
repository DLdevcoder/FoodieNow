package com.example.foodienow.feature.shipper_tracking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperTrackingScreen(
    onBack: () -> Unit,
    viewModel: ShipperTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val order by viewModel.currentOrder.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()

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

    val cameraPositionState = rememberCameraPositionState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Tùy chỉnh UI bản đồ (Tắt nút của Google)
    val mapUiSettings = remember {
        MapUiSettings(
            mapToolbarEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false
        )
    }

    DisposableEffect(hasLocationPermission) {
        var locationCallback: LocationCallback? = null

        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        viewModel.updateShipperLocation(location.latitude, location.longitude)
                        if (cameraPositionState.position.target.latitude == 0.0) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(location.latitude, location.longitude), 16f
                            )
                        }
                    }
                }
            }
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } catch (e: SecurityException) { }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        onDispose { locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) } }
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
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = mapUiSettings,
                    properties = MapProperties(isMyLocationEnabled = true)
                ) {
                    // Vẽ tuyến đường
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = MaterialTheme.colorScheme.primary,
                            width = 12f
                        )
                    }

                    if (order?.merchantLat != null && order?.merchantLng != null) {
                        Marker(
                            state = MarkerState(position = LatLng(order!!.merchantLat!!, order!!.merchantLng!!)),
                            title = "Quán ăn"
                        )
                    }

                    if (order?.deliveryLat != null && order?.deliveryLng != null) {
                        Marker(
                            state = MarkerState(position = LatLng(order!!.deliveryLat!!, order!!.deliveryLng!!)),
                            title = "Khách hàng"
                        )
                    }
                }

                // Panel thông tin đơn hàng ở cạnh dưới
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
                                onClick = { /* TODO: Xử lý hoàn thành đơn */ },
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