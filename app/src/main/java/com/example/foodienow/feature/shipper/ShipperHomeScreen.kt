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
import androidx.compose.ui.draw.clip
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
            containerColor = MaterialTheme.colorScheme.surface,
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

    Surface(
        color = Color.Transparent,
        contentColor = Color.White
    ) {
        Box(
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
                .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        modifier = Modifier.size(27.dp),
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitleText = if (activeOrderCount > 0) {
                        "Tự động nhận đơn • $activeOrderCount đơn"
                    } else {
                        "Tự động nhận đơn"
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

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
    val uiState by viewModel.uiState.collectAsState()
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    val formattedPrice = "${formatter.format(order.totalPrice)} VND"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        OrderStatus.WAITING_SHIPPER -> {
                            // 2. SỬ DỤNG uiState ĐÃ COLLECT ĐỂ KIỂM TRA
                            val isProcessing = uiState.processingOrderIds.contains(order.id)

                            Button(
                                onClick = { order.id?.let { viewModel.acceptOrder(it) } },
                                shape = MaterialTheme.shapes.medium,
                                enabled = !isProcessing // Khóa nút khi đang gửi lên server
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.shipper_action_accept))
                                }
                            }
                        }
                        OrderStatus.PICKING_UP -> {
                            OutlinedButton(
                                onClick = onNavigateToMap,
                                modifier = Modifier.padding(end = 8.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Bản đồ")
                            }
                            Button(
                                onClick = { order.id?.let { viewModel.markOrderAsPickedUp(it) } },
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

                            if (order.shipperConfirmed) {
                                Button(
                                    onClick = { },
                                    enabled = false, // Disable nút
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Chờ khách xác nhận")
                                }
                            } else {
                                Button(
                                    onClick = { order.id?.let { viewModel.completeOrder(it) } },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Đã giao")
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}