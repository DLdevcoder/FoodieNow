package com.example.foodienow.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieCreamSurface
import com.example.foodienow.core.designsystem.theme.OrangePrimary
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.core.designsystem.theme.ErrorRed
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.model.UserRole
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBack: () -> Unit,
    onNavigateToPaymentSettings: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }
    
    var topUpAmountText by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf(WalletProvider.MOMO) }

    var withdrawAmountText by remember { mutableStateOf("") }
    var selectedWithdrawWallet by remember { mutableStateOf("") }
    var localWithdrawError by remember { mutableStateOf<String?>(null) }
    
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }
    var activeBanner by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(uiState.linkedWallets) {
        if (selectedWithdrawWallet.isEmpty() && uiState.linkedWallets.isNotEmpty()) {
            selectedWithdrawWallet = uiState.linkedWallets.first()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage
        if (msg != null) {
            topUpAmountText = ""
            withdrawAmountText = ""
            activeBanner = Pair("Ví FoodiePay", msg)
            kotlinx.coroutines.delay(4000)
            activeBanner = null
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val msg = uiState.errorMessage
        if (msg != null) {
            activeBanner = Pair("Lỗi ví", msg)
            kotlinx.coroutines.delay(4000)
            activeBanner = null
        }
    }

    Scaffold(
        containerColor = FoodieCream,
        topBar = {
            TopAppBar(
                title = { Text("Ví FoodiePay", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FoodieCream,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    OrangePrimary,
                                    Color(0xFFFF8C42),
                                    Color(0xFFF25C54)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .offset(x = 180.dp, y = (-50).dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = 220.dp, y = 80.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "FOODIE PAY",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "Premium Account Wallet",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "SỐ DƯ KHẢ DỤNG",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatter.format(uiState.balance),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            if (uiState.userRole == UserRole.MERCHANT || uiState.userRole == UserRole.SHIPPER) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf("Nạp tiền", "Rút tiền")
                        tabs.forEachIndexed { index, label ->
                            val isSelected = activeTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) OrangePrimary else Color.Transparent)
                                    .clickable {
                                        activeTab = index
                                        viewModel.clearMessages()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            if (activeTab == 0 || uiState.userRole == UserRole.CUSTOMER) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Nạp tiền vào ví",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf(50000L, 100000L, 200000L, 500000L)
                            presets.forEach { preset ->
                                val isSelected = topUpAmountText == preset.toString()
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            topUpAmountText = preset.toString()
                                            viewModel.clearMessages()
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) OrangePrimary else Color.White
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${preset / 1000}K",
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = topUpAmountText,
                            onValueChange = { value ->
                                topUpAmountText = value.filter { it.isDigit() }
                                viewModel.clearMessages()
                            },
                            label = { Text("Nhập số tiền khác cần nạp") },
                            placeholder = { Text("Ví dụ: 100.000") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                focusedLabelColor = OrangePrimary,
                                cursorColor = OrangePrimary
                            )
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Chọn nguồn tiền thanh toán",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WalletProvider.entries.forEach { provider ->
                                val isSelected = selectedProvider == provider
                                Box(
                                    modifier = Modifier
                                        .size(width = 100.dp, height = 72.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) OrangePrimary.copy(alpha = 0.08f) else Color.White)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { selectedProvider = provider }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        val iconRes = when (provider) {
                                            WalletProvider.MOMO -> R.drawable.ic_momo
                                            WalletProvider.ZALOPAY -> R.drawable.ic_zalopay
                                            WalletProvider.VNPAY -> R.drawable.ic_vnpay
                                            WalletProvider.PAYPAL -> R.drawable.ic_paypal
                                        }
                                        Image(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when (provider) {
                                                WalletProvider.MOMO -> "MoMo"
                                                WalletProvider.ZALOPAY -> "ZaloPay"
                                                WalletProvider.VNPAY -> "VNPAY"
                                                WalletProvider.PAYPAL -> "PayPal"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(14.dp)
                                                .background(OrangePrimary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.errorMessage?.let {
                            Text(text = it, color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        uiState.successMessage?.let { msg ->
                            Text(text = msg, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        
                        Button(
                            onClick = {
                                val amount = topUpAmountText.toLongOrNull() ?: 0L
                                viewModel.topUp(amount, selectedProvider)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !uiState.isProcessing && topUpAmountText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang xử lý nạp tiền...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Nạp tiền ngay", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Rút tiền về ví liên kết",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        if (uiState.linkedWallets.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Bạn chưa liên kết ví điện tử nào để rút tiền.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = onNavigateToPaymentSettings,
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Liên kết ngay", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Chọn ví nhận tiền:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.linkedWallets.forEach { walletId ->
                                    val name = when (walletId) {
                                        "momo" -> "MoMo"
                                        "zalopay" -> "ZaloPay"
                                        "vnpay" -> "VNPAY"
                                        "paypal" -> "PayPal"
                                        else -> walletId.uppercase()
                                    }
                                    val isSelected = selectedWithdrawWallet == walletId
                                    OutlinedButton(
                                        onClick = { selectedWithdrawWallet = walletId },
                                        border = BorderStroke(
                                            1.5.dp,
                                            if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) OrangePrimary.copy(alpha = 0.1f) else Color.Transparent,
                                            contentColor = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = withdrawAmountText,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) {
                                        withdrawAmountText = input
                                        localWithdrawError = null
                                    }
                                },
                                label = { Text("Số tiền cần rút (₫)") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = localWithdrawError != null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    focusedLabelColor = OrangePrimary,
                                    cursorColor = OrangePrimary
                                )
                            )
                            
                            localWithdrawError?.let { err ->
                                Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            
                            uiState.errorMessage?.let { err ->
                                Text(text = err, color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            uiState.successMessage?.let { msg ->
                                Text(text = msg, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            
                            Button(
                                onClick = {
                                    val amount = withdrawAmountText.toLongOrNull() ?: 0L
                                    if (amount <= 0) {
                                        localWithdrawError = "Vui lòng nhập số tiền rút hợp lệ."
                                    } else if (amount > uiState.balance) {
                                        localWithdrawError = "Số dư không đủ để thực hiện rút tiền."
                                    } else if (amount < 10000L) {
                                        localWithdrawError = "Số tiền rút tối thiểu là 10.000 ₫."
                                    } else {
                                        val walletName = when (selectedWithdrawWallet) {
                                            "momo" -> "MoMo"
                                            "zalopay" -> "ZaloPay"
                                            "vnpay" -> "VNPAY"
                                            "paypal" -> "PayPal"
                                            else -> selectedWithdrawWallet.uppercase()
                                        }
                                        viewModel.withdraw(amount, walletName)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = !uiState.isProcessing && withdrawAmountText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Đang xử lý rút tiền...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Rút tiền ngay", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Lịch sử giao dịch",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (uiState.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chưa có lịch sử giao dịch.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(uiState.transactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val isTopUp = tx.type == com.example.foodienow.domain.model.WalletTransactionType.TOP_UP
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    color = if (isTopUp) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTopUp) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isTopUp) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val dateStr = runCatching {
                                val instant = java.time.Instant.parse(tx.createdAt)
                                val zoned = instant.atZone(java.time.ZoneId.systemDefault())
                                val dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                dtf.format(zoned)
                            }.getOrDefault(tx.createdAt.take(19).replace("T", " "))
                            
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        
                        Text(
                            text = if (isTopUp) "+${formatter.format(tx.amount)}" else "-${formatter.format(tx.amount)}",
                            color = if (isTopUp) SuccessGreen else ErrorRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    activeBanner?.let { (title, msg) ->
        androidx.compose.ui.window.Popup(
            alignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(OrangePrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                        Text(text = msg, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
