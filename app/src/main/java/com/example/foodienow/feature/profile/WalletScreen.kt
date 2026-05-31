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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

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
    var selectedTransactionForDetail by remember { mutableStateOf<com.example.foodienow.domain.model.WalletTransaction?>(null) }

    LaunchedEffect(uiState.linkedWallets) {
        if (selectedWithdrawWallet.isEmpty() && uiState.linkedWallets.isNotEmpty()) {
            selectedWithdrawWallet = uiState.linkedWallets.first()
        }
        if (uiState.linkedWallets.isNotEmpty()) {
            val firstLinked = uiState.linkedWallets.first()
            selectedProvider = when (firstLinked) {
                "momo" -> WalletProvider.MOMO
                "zalopay" -> WalletProvider.ZALOPAY
                "vnpay" -> WalletProvider.VNPAY
                "paypal" -> WalletProvider.PAYPAL
                else -> WalletProvider.MOMO
            }
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
                title = { Text("Ví FoodiePay", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FoodieCream,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), clip = false)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF5A1F),
                                    Color(0xFFFF8E3C),
                                    Color(0xFFFF0055)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .offset(x = 180.dp, y = (-60).dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .offset(x = 220.dp, y = 90.dp)
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
                                    fontSize = 20.sp,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "Platinum Card",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF39C12))
                                    .border(1.5.dp, Color(0xFFF1C40F), RoundedCornerShape(6.dp))
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color(0xFFD68910).copy(alpha = 0.4f)))
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color(0xFFD68910).copy(alpha = 0.4f)))
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color(0xFFD68910).copy(alpha = 0.4f)))
                                }
                            }
                        }
                        
                        Column {
                            Text(
                                text = "SỐ DƯ KHẢ DỤNG",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatter.format(uiState.balance),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "•••• •••• •••• 8888",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.userRole == UserRole.CUSTOMER || uiState.userRole == UserRole.MERCHANT || uiState.userRole == UserRole.SHIPPER || uiState.userRole == UserRole.ADMIN) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)), RoundedCornerShape(18.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf("Nạp tiền", "Rút tiền")
                        tabs.forEachIndexed { index, label ->
                            val isSelected = activeTab == index
                            val backgroundModifier = if (isSelected) {
                                Modifier
                                    .weight(1f)
                                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFF5A1F),
                                                Color(0xFFFF8E3C)
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            } else {
                                Modifier
                                    .weight(1f)
                                    .background(Color.Transparent)
                            }

                            Box(
                                modifier = backgroundModifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        activeTab = index
                                        viewModel.clearMessages()
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            if (activeTab == 0) {
                if (uiState.linkedWallets.isEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Nạp tiền vào ví",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Bạn chưa liên kết ví điện tử nào để nạp tiền.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = onNavigateToPaymentSettings,
                                        modifier = Modifier.height(46.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Liên kết ngay", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Nạp tiền vào ví",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val presets = listOf(50000L, 100000L, 200000L, 500000L)
                                presets.forEach { preset ->
                                    val isSelected = topUpAmountText == preset.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .shadow(elevation = if (isSelected) 6.dp else 1.dp, shape = RoundedCornerShape(16.dp))
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) OrangePrimary else MaterialTheme.colorScheme.surface)
                                            .border(
                                                BorderStroke(
                                                    1.5.dp,
                                                    if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                                ),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                topUpAmountText = preset.toString()
                                                viewModel.clearMessages()
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${preset / 1000}K",
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = topUpAmountText,
                                onValueChange = { value ->
                                    topUpAmountText = value.filter { it.isDigit() }
                                    viewModel.clearMessages()
                                },
                                label = { Text("Số tiền cần nạp (₫)", fontWeight = FontWeight.Bold) },
                                placeholder = { Text("Ví dụ: 100.000") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                suffix = {
                                    Text("₫", fontWeight = FontWeight.Bold, color = OrangePrimary)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    focusedLabelColor = OrangePrimary,
                                    cursorColor = OrangePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Chọn nguồn tiền thanh toán",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val linkedProviders = WalletProvider.entries.filter { provider ->
                                    val providerId = when (provider) {
                                        WalletProvider.MOMO -> "momo"
                                        WalletProvider.ZALOPAY -> "zalopay"
                                        WalletProvider.VNPAY -> "vnpay"
                                        WalletProvider.PAYPAL -> "paypal"
                                    }
                                    uiState.linkedWallets.contains(providerId)
                                }
                                linkedProviders.forEach { provider ->
                                    val isSelected = selectedProvider == provider
                                    Box(
                                        modifier = Modifier
                                            .size(width = 110.dp, height = 80.dp)
                                            .shadow(
                                                elevation = if (isSelected) 8.dp else 2.dp,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedProvider = provider }
                                            .padding(10.dp),
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
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = when (provider) {
                                                    WalletProvider.MOMO -> "MoMo"
                                                    WalletProvider.ZALOPAY -> "ZaloPay"
                                                    WalletProvider.VNPAY -> "VNPAY"
                                                    WalletProvider.PAYPAL -> "PayPal"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 2.dp, y = (-2).dp)
                                                    .size(18.dp)
                                                    .background(OrangePrimary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !uiState.isProcessing && topUpAmountText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary,
                                    disabledContainerColor = Color(0xFFEBE3DB),
                                    contentColor = Color.White,
                                    disabledContentColor = Color(0xFF7A6E65)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp,
                                    disabledElevation = 0.dp
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (uiState.isProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = LocalContentColor.current, strokeWidth = 2.5.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Đang xử lý nạp tiền...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Nạp tiền ngay", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Rút tiền về ví liên kết",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        
                        if (uiState.linkedWallets.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Bạn chưa liên kết ví điện tử nào để rút tiền.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = onNavigateToPaymentSettings,
                                        modifier = Modifier.height(46.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Liên kết ngay", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Chọn ví nhận tiền:",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
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
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .shadow(elevation = if (isSelected) 6.dp else 1.dp, shape = RoundedCornerShape(16.dp))
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) OrangePrimary else MaterialTheme.colorScheme.surface)
                                            .border(
                                                BorderStroke(
                                                    1.5.dp,
                                                    if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                                ),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedWithdrawWallet = walletId }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
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
                                label = { Text("Số tiền cần rút (₫)", fontWeight = FontWeight.Bold) },
                                placeholder = { Text("Ví dụ: 50.000") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                suffix = {
                                    Text("₫", fontWeight = FontWeight.Bold, color = OrangePrimary)
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = localWithdrawError != null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    focusedLabelColor = OrangePrimary,
                                    cursorColor = OrangePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !uiState.isProcessing && withdrawAmountText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary,
                                    disabledContainerColor = Color(0xFFEBE3DB),
                                    contentColor = Color.White,
                                    disabledContentColor = Color(0xFF7A6E65)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp,
                                    disabledElevation = 0.dp
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (uiState.isProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = LocalContentColor.current, strokeWidth = 2.5.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Đang xử lý rút tiền...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Rút tiền ngay", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lịch sử giao dịch",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF5E534B),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (uiState.transactions.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có lịch sử giao dịch.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            if (uiState.transactions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column {
                            uiState.transactions.forEachIndexed { index, tx ->
                                val isTopUp = tx.type == com.example.foodienow.domain.model.WalletTransactionType.TOP_UP ||
                                        tx.type == com.example.foodienow.domain.model.WalletTransactionType.REFUND
                                val isRefund = tx.type == com.example.foodienow.domain.model.WalletTransactionType.REFUND
                                val isWithdraw = tx.type == com.example.foodienow.domain.model.WalletTransactionType.WITHDRAW
                                
                                val iconBgColor = when {
                                    isRefund -> Color(0xFFE8EFFF)
                                    isTopUp -> Color(0xFFEAF8F1)
                                    isWithdraw -> Color(0xFFFFF0F0)
                                    else -> Color(0xFFFFF4EC)
                                }
                                val iconColor = when {
                                    isRefund -> Color(0xFF3B82F6)
                                    isTopUp -> SuccessGreen
                                    isWithdraw -> ErrorRed
                                    else -> OrangePrimary
                                }
                                val iconVector = when {
                                    isRefund -> Icons.Default.History
                                    isTopUp -> Icons.Default.ArrowDownward
                                    isWithdraw -> Icons.Default.ArrowUpward
                                    else -> Icons.Default.CreditCard
                                }
                                val badgeText = when (tx.type) {
                                    com.example.foodienow.domain.model.WalletTransactionType.TOP_UP -> "Nạp tiền"
                                    com.example.foodienow.domain.model.WalletTransactionType.PAYMENT -> "Thanh toán"
                                    com.example.foodienow.domain.model.WalletTransactionType.WITHDRAW -> "Rút tiền"
                                    com.example.foodienow.domain.model.WalletTransactionType.REFUND -> "Hoàn tiền"
                                }

                                val cleanedDesc = tx.description.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
                                    .replace("Thanh toan don hang", "Thanh toán đơn hàng", ignoreCase = true)
                                    .replace("Nhan thanh toan don hang", "Nhận thanh toán đơn hàng", ignoreCase = true)
                                    .replace("Thanh toan Merchant don hang", "Thanh toán Merchant đơn hàng", ignoreCase = true)
                                    .replace("Thanh toan Shipper don hang", "Thanh toán Shipper đơn hàng", ignoreCase = true)
                                    .replace("Thanh toan", "Thanh toán", ignoreCase = true)
                                    .replace("don hang", "đơn hàng", ignoreCase = true)
                                    .replace("Nhan thanh toan", "Nhận thanh toán", ignoreCase = true)

                                val dateStr = runCatching {
                                    val instant = java.time.Instant.parse(tx.createdAt)
                                    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
                                    val dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                    dtf.format(zoned)
                                }.getOrDefault(tx.createdAt.take(19).replace("T", " "))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTransactionForDetail = tx }
                                        .padding(vertical = 14.dp, horizontal = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(iconBgColor, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cleanedDesc,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(iconColor.copy(alpha = 0.08f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = badgeText,
                                                    color = iconColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Text(
                                                text = dateStr,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (isTopUp) "+${formatter.format(tx.amount)}" else "-${formatter.format(tx.amount)}",
                                        color = when {
                                            isTopUp -> SuccessGreen
                                            isWithdraw -> ErrorRed
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }

                                if (index < uiState.transactions.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 18.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = msg, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (selectedTransactionForDetail != null) {
        val tx = selectedTransactionForDetail!!
        val isTopUp = tx.type == com.example.foodienow.domain.model.WalletTransactionType.TOP_UP ||
                tx.type == com.example.foodienow.domain.model.WalletTransactionType.REFUND
        val isRefund = tx.type == com.example.foodienow.domain.model.WalletTransactionType.REFUND
        val isWithdraw = tx.type == com.example.foodienow.domain.model.WalletTransactionType.WITHDRAW
        
        val iconBgColor = when {
            isRefund -> Color(0xFFE8EFFF)
            isTopUp -> Color(0xFFEAF8F1)
            isWithdraw -> Color(0xFFFFF0F0)
            else -> Color(0xFFFFF4EC)
        }
        val iconColor = when {
            isRefund -> Color(0xFF3B82F6)
            isTopUp -> SuccessGreen
            isWithdraw -> ErrorRed
            else -> OrangePrimary
        }
        val iconVector = when {
            isRefund -> Icons.Default.History
            isTopUp -> Icons.Default.ArrowDownward
            isWithdraw -> Icons.Default.ArrowUpward
            else -> Icons.Default.CreditCard
        }
        val badgeText = when (tx.type) {
            com.example.foodienow.domain.model.WalletTransactionType.TOP_UP -> "Nạp tiền"
            com.example.foodienow.domain.model.WalletTransactionType.PAYMENT -> "Thanh toán"
            com.example.foodienow.domain.model.WalletTransactionType.WITHDRAW -> "Rút tiền"
            com.example.foodienow.domain.model.WalletTransactionType.REFUND -> "Hoàn tiền"
        }
        
        val fullDateStr = runCatching {
            val instant = java.time.Instant.parse(tx.createdAt)
            val zoned = instant.atZone(java.time.ZoneId.systemDefault())
            val dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            dtf.format(zoned)
        }.getOrDefault(tx.createdAt)

        ModalBottomSheet(
            onDismissRequest = { selectedTransactionForDetail = null },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Text(
                    text = badgeText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = iconColor
                )

                Text(
                    text = if (isTopUp) "+${formatter.format(tx.amount)}" else "-${formatter.format(tx.amount)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = when {
                        isTopUp -> SuccessGreen
                        isWithdraw -> ErrorRed
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FoodieCreamSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mã giao dịch", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = tx.id.take(12) + if (tx.id.length > 12) "..." else "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sao chép",
                                    color = OrangePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(OrangePrimary.copy(alpha = 0.08f))
                                        .clickable {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(tx.id))
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trạng thái", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Thành công",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Thời gian", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = fullDateStr,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Mô tả", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = tx.description.replace("\n", " ").trim()
                                    .replace("Thanh toan don hang", "Thanh toán đơn hàng", ignoreCase = true)
                                    .replace("Nhan thanh toan don hang", "Nhận thanh toán đơn hàng", ignoreCase = true)
                                    .replace("Thanh toan Merchant don hang", "Thanh toán Merchant đơn hàng", ignoreCase = true)
                                    .replace("Thanh toan Shipper don hang", "Thanh toán Shipper đơn hàng", ignoreCase = true)
                                    .replace("Thanh toan", "Thanh toán", ignoreCase = true)
                                    .replace("don hang", "đơn hàng", ignoreCase = true)
                                    .replace("Nhan thanh toan", "Nhận thanh toán", ignoreCase = true),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { selectedTransactionForDetail = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đóng", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}
