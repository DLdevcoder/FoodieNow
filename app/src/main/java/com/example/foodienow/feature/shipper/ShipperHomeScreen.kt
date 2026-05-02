package com.example.foodienow.feature.shipper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.domain.model.Order
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperHomeScreen(
    viewModel: ShipperViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài xế FoodieNow") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Button(onClick = onLogout) {
                        Text("Đăng xuất", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.availableOrders.isEmpty() && uiState.activeOrder == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (uiState.activeOrder != null) {
                    ActiveDeliverySection(
                        order = uiState.activeOrder!!,
                        onComplete = { viewModel.completeOrder(it) }
                    )
                } else {
                    AvailableDeliveriesSection(
                        orders = uiState.availableOrders,
                        onAccept = { viewModel.acceptOrder(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveDeliverySection(order: Order, onComplete: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Đơn đang giao",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mã đơn: ${order.id?.take(8)?.uppercase() ?: ""}", fontWeight = FontWeight.Bold)
                Text("Trạng thái: ${order.status}", color = MaterialTheme.colorScheme.primary)
                Text("Tổng tiền: ${NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(order.totalPrice)}")
                Text("Địa chỉ giao: ${order.deliveryAddress}")
                
                Button(
                    onClick = { order.id?.let { onComplete(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Đã giao thành công")
                }
            }
        }
    }
}

@Composable
fun AvailableDeliveriesSection(orders: List<Order>, onAccept: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Đơn mới có thể nhận",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có đơn hàng mới nào.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Mã đơn: ${order.id?.take(8)?.uppercase() ?: ""}", fontWeight = FontWeight.Bold)
                            Text("Tổng tiền: ${NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(order.totalPrice)}")
                            Text("Địa chỉ: ${order.deliveryAddress}")
                            
                            Button(
                                onClick = { order.id?.let { onAccept(it) } },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text("Nhận đơn này")
                            }
                        }
                    }
                }
            }
        }
    }
}
