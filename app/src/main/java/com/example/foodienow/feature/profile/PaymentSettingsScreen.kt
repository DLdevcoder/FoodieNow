package com.example.foodienow.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.domain.payment.PaymentMethodCatalog

data class PaymentSettingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val requiresSetup: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSettingsScreen(
    onBack: () -> Unit,
    viewModel: PaymentSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var setupTarget by remember { mutableStateOf<PaymentSettingItem?>(null) }
    var removeTarget by remember { mutableStateOf<PaymentSettingItem?>(null) }

    val paymentMethods = remember {
        listOf(
            PaymentSettingItem(
                id = PaymentMethodCatalog.COD_ID,
                title = "Thanh toán tiền mặt",
                subtitle = "Thanh toán khi nhận hàng",
                icon = Icons.Default.Money,
                requiresSetup = false
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.CARD_ID,
                title = "Thẻ tín dụng / ghi nợ",
                subtitle = "Lưu chủ thẻ và 4 số cuối",
                icon = Icons.Default.CreditCard,
                requiresSetup = true
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.FOODIE_PAY_ID,
                title = "Ví FoodiePay",
                subtitle = "Dùng số dư FoodiePay",
                icon = Icons.Default.AccountBalanceWallet,
                requiresSetup = false
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.MOMO_ID,
                title = "Ví MoMo",
                subtitle = "Liên kết số điện thoại hoặc tài khoản",
                icon = Icons.Default.AccountBalanceWallet,
                requiresSetup = true
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.ZALOPAY_ID,
                title = "ZaloPay",
                subtitle = "Liên kết số điện thoại hoặc tài khoản",
                icon = Icons.Default.AccountBalanceWallet,
                requiresSetup = true
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.VNPAY_ID,
                title = "VNPAY",
                subtitle = "Liên kết tài khoản thanh toán",
                icon = Icons.Default.AccountBalanceWallet,
                requiresSetup = true
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.PAYPAL_ID,
                title = "PayPal",
                subtitle = "Liên kết email PayPal",
                icon = Icons.Default.AccountBalanceWallet,
                requiresSetup = true
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.GOOGLE_PLAY_ID,
                title = "Google Play",
                subtitle = "Liên kết tài khoản Google Play",
                icon = Icons.Default.AccountBalanceWallet,
                requiresSetup = true
            )
        )
    }

    val defaultMethodId = PaymentSettingsSelectionMapper.toOptionId(
        method = settings.defaultMethod,
        provider = settings.defaultProvider
    )

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
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
                )
            }
            uiState.infoMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paymentMethods, key = { it.id }) { method ->
                    val info = settings.methodInfos[method.id]
                    val isAvailable = settings.isOptionAvailable(method.id)
                    val isDefault = defaultMethodId == method.id

                    PaymentSettingCard(
                        method = method,
                        details = info?.details,
                        isAvailable = isAvailable,
                        isDefault = isDefault,
                        isSaving = uiState.isSaving,
                        onSetDefault = {
                            viewModel.clearMessage()
                            if (isAvailable) {
                                viewModel.updateDefaultMethod(method.id)
                            } else {
                                setupTarget = method
                            }
                        },
                        onSetup = {
                            viewModel.clearMessage()
                            setupTarget = method
                        },
                        onRemove = {
                            viewModel.clearMessage()
                            removeTarget = method
                        }
                    )
                }
            }
        }
    }

    setupTarget?.let { method ->
        SetupPaymentMethodDialog(
            method = method,
            isSaving = uiState.isSaving,
            onDismiss = { setupTarget = null },
            onSave = { details ->
                viewModel.savePaymentMethodInfo(
                    optionId = method.id,
                    displayName = method.title,
                    details = details
                )
                setupTarget = null
            }
        )
    }

    removeTarget?.let { method ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("Gỡ thông tin thanh toán") },
            text = {
                Text(
                    "Gỡ thông tin ${method.title}? Nếu đây là phương thức mặc định, hệ thống sẽ chuyển về tiền mặt."
                )
            },
            confirmButton = {
                Button(
                    enabled = !uiState.isSaving,
                    onClick = {
                        viewModel.removePaymentMethodInfo(method.id)
                        removeTarget = null
                    }
                ) {
                    Text("Gỡ")
                }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun PaymentSettingCard(
    method: PaymentSettingItem,
    details: String?,
    isAvailable: Boolean,
    isDefault: Boolean,
    isSaving: Boolean,
    onSetDefault: () -> Unit,
    onSetup: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSetDefault,
        colors = CardDefaults.cardColors(
            containerColor = if (isDefault) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(method.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = method.title, fontWeight = FontWeight.Bold)
                    Text(text = details ?: method.subtitle, style = MaterialTheme.typography.bodyMedium)
                    if (method.requiresSetup && !isAvailable) {
                        Text(
                            text = "Chưa cài đặt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (isDefault) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Mặc định",
                        tint = Color(0xFF10B981)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (method.requiresSetup && !isAvailable) {
                    Button(
                        enabled = !isSaving,
                        onClick = onSetup
                    ) {
                        Text("Cài đặt")
                    }
                } else {
                    OutlinedButton(
                        enabled = !isSaving && !isDefault,
                        onClick = onSetDefault
                    ) {
                        Text(if (isDefault) "Đang mặc định" else "Đặt mặc định")
                    }
                    if (method.requiresSetup) {
                        TextButton(
                            enabled = !isSaving,
                            onClick = onRemove
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gỡ")
                        }
                    }
                }
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.width(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SetupPaymentMethodDialog(
    method: PaymentSettingItem,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var primaryInput by remember(method.id) { mutableStateOf("") }
    var lastFourInput by remember(method.id) { mutableStateOf("") }
    val isCard = method.id == PaymentMethodCatalog.CARD_ID
    val normalizedLastFour = lastFourInput.filter { it.isDigit() }.takeLast(4)
    val canSave = if (isCard) {
        primaryInput.isNotBlank() && normalizedLastFour.length == 4
    } else {
        primaryInput.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cài đặt ${method.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isCard) {
                    OutlinedTextField(
                        value = primaryInput,
                        onValueChange = { primaryInput = it },
                        label = { Text("Tên chủ thẻ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastFourInput,
                        onValueChange = { lastFourInput = it.filter(Char::isDigit).take(4) },
                        label = { Text("4 số cuối trên thẻ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = primaryInput,
                        onValueChange = { primaryInput = it },
                        label = { Text("Số điện thoại, email hoặc mã tài khoản") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canSave && !isSaving,
                onClick = {
                    val details = if (isCard) {
                        "Chủ thẻ ${primaryInput.trim()} - **** $normalizedLastFour"
                    } else {
                        "${method.title} - ${primaryInput.trim()}"
                    }
                    onSave(details)
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
