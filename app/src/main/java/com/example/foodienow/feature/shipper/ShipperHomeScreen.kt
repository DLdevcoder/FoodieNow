package com.example.foodienow.feature.shipper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ShipperHomeScreen(
    viewModel: ShipperViewModel = hiltViewModel(),
    onNavigateToTracking: (String) -> Unit,
    onLogout: () -> Unit,
    initialTabIndex: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember(initialTabIndex) { mutableIntStateOf(initialTabIndex) }
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    DisposableEffect(hasLocationPermission) {
        var locationCallback: LocationCallback? = null
        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        viewModel.updateLocation(location.latitude, location.longitude)
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

    val tabs = listOf(
        R.string.shipper_tab_available,
        R.string.shipper_tab_active,
        R.string.shipper_tab_completed
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodieCream)
    ) {
        ShipperTopSection(
            activeOrderCount = uiState.activeOrders.size,
            isAutoAcceptEnabled = uiState.isAutoAcceptEnabled,
            onToggleAutoAccept = { viewModel.toggleAutoAccept(it) }
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, titleRes ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = stringResource(titleRes),
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        uiState.error?.let { errorMsg ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
            }
            return
        }

        if (!hasLocationPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Cần quyền vị trí để lấy đơn gần bạn", color = MaterialTheme.colorScheme.error)
            }
            return
        }

        when (selectedTabIndex) {
            0 -> OrderList(
                orders = uiState.availableOrders,
                emptyMessageRes = R.string.shipper_empty_available,
                onNavigateToMapClick = { },
                viewModel = viewModel,
                isHistoryTab = false
            )
            1 -> OrderList(
                orders = uiState.activeOrders,
                emptyMessageRes = R.string.shipper_empty_active,
                onNavigateToMapClick = onNavigateToTracking,
                viewModel = viewModel,
                isHistoryTab = false
            )
            2 -> OrderList(
                orders = uiState.completedOrders,
                emptyMessageRes = R.string.shipper_empty_completed,
                onNavigateToMapClick = { },
                viewModel = viewModel,
                isHistoryTab = true
            )
        }
    }
}

@Composable
private fun ShipperTopSection(
    activeOrderCount: Int,
    isAutoAcceptEnabled: Boolean,
    onToggleAutoAccept: (Boolean) -> Unit
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Chào buổi sáng ☀️"
        in 12..17 -> "Chào buổi chiều ⛅"
        else -> "Chào buổi tối 🌙"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PromoGradientStart,
                        MaterialTheme.colorScheme.primary,
                        PromoGradientEnd
                    )
                )
            )
            .statusBarsPadding()
            .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tự động nhận đơn",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isAutoAcceptEnabled,
                        onCheckedChange = onToggleAutoAccept,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (activeOrderCount > 0) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$activeOrderCount đơn",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderList(
    orders: List<Order>,
    emptyMessageRes: Int,
    onNavigateToMapClick: (String) -> Unit,
    viewModel: ShipperViewModel,
    isHistoryTab: Boolean
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(emptyMessageRes),
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.id ?: it.hashCode() }) { order ->
                ShipperOrderCard(
                    order = order,
                    onNavigateToMap = { order.id?.let { onNavigateToMapClick(it) } },
                    viewModel = viewModel,
                    isHistoryTab = isHistoryTab
                )
            }
        }
    }
}
@Composable
private fun ShipperOrderCard(
    order: Order,
    onNavigateToMap: () -> Unit,
    viewModel: ShipperViewModel,
    isHistoryTab: Boolean
) {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    val formattedPrice = "${formatter.format(order.totalPrice)} VND"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn #${order.id?.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Giao đến: ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!order.note.isNullOrBlank()) {
                Text(
                    text = "Ghi chú: ${order.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isHistoryTab || order.status == OrderStatus.COMPLETED) {
                    Text(
                        text = "Đã giao thành công",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    when (order.status) {
                        OrderStatus.PREPARING -> {
                            Button(
                                onClick = { order.id?.let { viewModel.acceptOrder(it) } },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.shipper_action_accept))
                            }
                        }
                        OrderStatus.DRIVER_ASSIGNED -> {
                            OutlinedButton(
                                onClick = { order.id?.let { viewModel.cancelOrder(it) } },
                                modifier = Modifier.padding(end = 8.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFD32F2F)
                                )
                            ) {
                                Text("Hủy đơn")
                            }
                            OutlinedButton(
                                onClick = onNavigateToMap,
                                modifier = Modifier.padding(end = 8.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Bản đồ")
                            }
                            Button(
                                onClick = { order.id?.let { viewModel.markAsDelivering(it) } },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Đã lấy hàng")
                            }
                        }
                        OrderStatus.DELIVERING -> {
                            OutlinedButton(
                                onClick = onNavigateToMap,
                                modifier = Modifier.padding(end = 8.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Bản đồ")
                            }

                            val isConfirmed = order.shipperConfirmed

                            Button(
                                onClick = { order.id?.let { viewModel.completeOrder(it) } },
                                shape = MaterialTheme.shapes.medium,
                                enabled = !isConfirmed,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isConfirmed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isConfirmed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(if (isConfirmed) "Chờ khách xác nhận" else "Hoàn thành")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}