package com.example.foodienow.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PaymentSettingItem(val id: String, val title: String, val subtitle: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSettingsScreen(
    onBack: () -> Unit,
    viewModel: PaymentSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    
    val paymentMethods = listOf(
        PaymentSettingItem("momo", "Ví MoMo", "Liên kết: 0912***456", Icons.Default.AccountBalanceWallet),
        PaymentSettingItem("zalopay", "ZaloPay", "Chưa liên kết", Icons.Default.AccountBalanceWallet),
        PaymentSettingItem("card", "Thẻ Tín dụng / Ghi nợ", "Thêm thẻ mới", Icons.Default.CreditCard),
        PaymentSettingItem("cod", "Thanh toán tiền mặt", "Thanh toán khi nhận hàng", Icons.Default.Money)
    )

    val defaultMethodId = when {
        settings.defaultMethod == PaymentMethod.WALLET && settings.defaultProvider == WalletProvider.MOMO -> "momo"
        settings.defaultMethod == PaymentMethod.WALLET && settings.defaultProvider == WalletProvider.ZALOPAY -> "zalopay"
        settings.defaultMethod == PaymentMethod.CARD -> "card"
        else -> "cod"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phương thức thanh toán") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paymentMethods) { method ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.updateDefaultMethod(method.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (defaultMethodId == method.id) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(method.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = method.title, fontWeight = FontWeight.Bold)
                                Text(text = method.subtitle, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (defaultMethodId == method.id) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Mặc định", tint = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
    }
}
