package com.example.foodienow.feature.shipper_tracking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val cameraPositionState = rememberCameraPositionState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    DisposableEffect(hasLocationPermission) {
        var locationCallback: LocationCallback? = null

        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateDistanceMeters(5f)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        viewModel.updateShipperLocation(location.latitude, location.longitude)

                        if (cameraPositionState.position.target.latitude == 0.0) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(location.latitude, location.longitude), 15f
                            )
                        }
                    }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) { e.printStackTrace() }
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }

        onDispose {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        }
    }

    // Bọc toàn bộ UI bằng Scaffold để có TopBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theo dõi & Dẫn đường", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding của Scaffold để không đè lên TopBar
        ) {
            if (!hasLocationPermission) {
                Text(
                    text = "Vui lòng cấp quyền vị trí để theo dõi đơn hàng",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true)
                ) {
                    if (order?.shipperLat != null && order?.shipperLng != null) {
                        Marker(
                            state = MarkerState(position = LatLng(order!!.shipperLat!!, order!!.shipperLng!!)),
                            title = "Vị trí của bạn",
                            snippet = "Đang giao hàng"
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
                            title = "Điểm giao",
                            snippet = order!!.deliveryAddress
                        )
                    }
                }
            }
        }
    }
}