package com.example.foodienow.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.domain.model.WalletProvider
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var topUpAmountText by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf(WalletProvider.MOMO) }
    
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            topUpAmountText = "" // Clear input on success
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ví FoodiePay", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
            // Khung số dư
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Số dư khả dụng",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = formatter.format(uiState.balance),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text("Nạp tiền vào ví", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = topUpAmountText,
                onValueChange = { value ->
                    topUpAmountText = value.filter { it.isDigit() }
                },
                label = { Text("Nhập số tiền cần nạp") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Text("Chọn nguồn tiền (Sandbox Simulator)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WalletProvider.entries.forEach { provider ->
                    FilterChip(
                        selected = selectedProvider == provider,
                        onClick = { selectedProvider = provider },
                        label = {
                            Text(
                                text = when (provider) {
                                    WalletProvider.ZALOPAY -> "ZaloPay"
                                    WalletProvider.MOMO -> "MoMo"
                                    WalletProvider.VNPAY -> "VNPAY"
                                    WalletProvider.PAYPAL -> "PayPal"
                                    WalletProvider.GOOGLE_PLAY -> "Google Play"
                                }
                            )
                        }
                    )
                }
            }

            uiState.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            uiState.successMessage?.let {
                Text(text = it, color = Color(0xFF10B981)) // Green
            }

            Button(
                onClick = {
                    val amount = topUpAmountText.toLongOrNull() ?: 0L
                    viewModel.topUp(amount, selectedProvider)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isProcessing && topUpAmountText.isNotBlank()
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang xử lý nạp tiền...")
                } else {
                    Text("Nạp tiền ngay", fontSize = 16.sp)
                }
            }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Lịch sử giao dịch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.transactions.isEmpty()) {
                    Text("Chưa có giao dịch nào.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            items(uiState.transactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = tx.description, fontWeight = FontWeight.Bold)
                            Text(text = tx.createdAt.take(19).replace("T", " "), style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = if (tx.type == com.example.foodienow.domain.model.WalletTransactionType.TOP_UP) "+${formatter.format(tx.amount)}" else "-${formatter.format(tx.amount)}",
                            color = if (tx.type == com.example.foodienow.domain.model.WalletTransactionType.TOP_UP) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
