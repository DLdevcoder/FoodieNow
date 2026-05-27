package com.example.foodienow.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.OrangePrimary
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.core.designsystem.theme.ErrorRed
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.AmberTertiary
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.domain.model.AdminAccountStats
import com.example.foodienow.domain.model.AdminFinancialStats
import com.example.foodienow.domain.model.AdminProfileStats
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.model.WalletTransactionType
import com.example.foodienow.feature.profile.ProfileScreen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToPaymentSettings: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    var showDetailUserDialog by remember { mutableStateOf<AdminProfileStats?>(null) }
    var newBalanceInput by remember { mutableStateOf("") }

    var commissionInput by remember { mutableStateOf("") }
    var deliveryFeeInput by remember { mutableStateOf("") }
    var freeDeliveryThresholdInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.systemSettings) {
        val rate = uiState.systemSettings.find { it.key == "admin_commission_rate" }?.value ?: 0.10
        commissionInput = (rate * 100).toString()
        val fee = uiState.systemSettings.find { it.key == "base_delivery_fee" }?.value ?: 15000.0
        deliveryFeeInput = fee.toLong().toString()
        val threshold = uiState.systemSettings.find { it.key == "free_delivery_threshold" }?.value ?: 100000.0
        freeDeliveryThresholdInput = threshold.toLong().toString()
    }

    LaunchedEffect(showDetailUserDialog) {
        if (showDetailUserDialog != null) {
            viewModel.loadUserTransactions(showDetailUserDialog!!.id)
            newBalanceInput = showDetailUserDialog!!.balance.toString()
        } else {
            viewModel.clearUserTransactions()
        }
    }

    if (showDetailUserDialog != null) {
        val profile = showDetailUserDialog!!
        AlertDialog(
            onDismissRequest = { showDetailUserDialog = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chi tiết tài khoản", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { showDetailUserDialog = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!profile.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                val iconColor = when (profile.role) {
                                    UserRole.CUSTOMER -> InfoBlue
                                    UserRole.MERCHANT -> AmberTertiary
                                    UserRole.SHIPPER -> SuccessGreen
                                    UserRole.ADMIN -> OrangePrimary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(iconColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (profile.role) {
                                            UserRole.CUSTOMER -> Icons.Default.Person
                                            UserRole.MERCHANT -> Icons.Default.Storefront
                                            UserRole.SHIPPER -> Icons.Default.DirectionsBike
                                            UserRole.ADMIN -> Icons.Default.SupervisorAccount
                                        },
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Column {
                                Text(profile.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(profile.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Badge(
                                    containerColor = when (profile.role) {
                                        UserRole.CUSTOMER -> InfoBlue.copy(alpha = 0.15f)
                                        UserRole.MERCHANT -> AmberTertiary.copy(alpha = 0.15f)
                                        UserRole.SHIPPER -> SuccessGreen.copy(alpha = 0.15f)
                                        UserRole.ADMIN -> OrangePrimary.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = profile.role.name,
                                        color = when (profile.role) {
                                            UserRole.CUSTOMER -> InfoBlue
                                            UserRole.MERCHANT -> AmberTertiary
                                            UserRole.SHIPPER -> SuccessGreen
                                            UserRole.ADMIN -> OrangePrimary
                                        },
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Số dư tài khoản", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hiện tại:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    formatter.format(profile.balance),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = OrangePrimary
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            Text("Thay đổi số dư:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newBalanceInput,
                                    onValueChange = { input -> newBalanceInput = input.filter { it.isDigit() } },
                                    label = { Text("Số dư mới (₫)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        val amt = newBalanceInput.toLongOrNull()
                                        if (amt != null) {
                                            viewModel.updateUserBalance(profile.id, amt)
                                        }
                                        showDetailUserDialog = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text("Lưu", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Lịch sử giao dịch ví", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        if (uiState.isTransactionsLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = OrangePrimary)
                            }
                        } else if (uiState.userTransactions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Chưa có lịch sử giao dịch nào.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.userTransactions.forEach { tx ->
                                        val isPositive = tx.type == WalletTransactionType.TOP_UP || tx.type == WalletTransactionType.REFUND
                                        val amountText = (if (isPositive) "+" else "-") + formatter.format(tx.amount)
                                        val amountColor = if (isPositive) SuccessGreen else ErrorRed
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp, horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(amountColor.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = when (tx.type) {
                                                            WalletTransactionType.TOP_UP -> Icons.Default.AccountBalanceWallet
                                                            WalletTransactionType.PAYMENT -> Icons.Default.ShoppingCart
                                                            WalletTransactionType.WITHDRAW -> Icons.Default.Payments
                                                            WalletTransactionType.REFUND -> Icons.Default.History
                                                        },
                                                        contentDescription = null,
                                                        tint = amountColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = tx.description,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = tx.createdAt.substringBefore("T"),
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Text(
                                                text = amountText,
                                                color = amountColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                        if (tx != uiState.userTransactions.last()) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        containerColor = FoodieCream,
        topBar = {
            if (activeTab != 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PromoGradientStart, MaterialTheme.colorScheme.primary, PromoGradientEnd)
                            )
                        )
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (activeTab == 0) "Quản lý tài khoản" else "Quản lý dòng tiền",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        modifier = Modifier.statusBarsPadding()
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                tonalElevation = 3.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(72.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    val tabs = listOf(
                        Triple(Icons.Default.SupervisorAccount, "Tài khoản", 0),
                        Triple(Icons.Default.MonetizationOn, "Dòng tiền", 1),
                        Triple(Icons.Default.Person, "Tôi", 2)
                    )

                    tabs.forEach { (icon, label, index) ->
                        val selected = activeTab == index
                        NavigationBarItem(
                            icon = {
                                Icon(icon, contentDescription = label)
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = selected,
                            onClick = {
                                activeTab = index
                                viewModel.clearMessages()
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (activeTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding() + 16.dp)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.errorMessage?.let { msg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = ErrorRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        uiState.successMessage?.let { msg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = SuccessGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        AccountsTabContent(
                            uiState = uiState,
                            formatter = formatter,
                            onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                            onRoleFilterChanged = { viewModel.updateRoleFilter(it) },
                            onSearchCriteriaChanged = { viewModel.updateSearchCriteria(it) },
                            onEditBalanceRequested = { profile ->
                                showDetailUserDialog = profile
                            },
                            onRefresh = {
                                viewModel.loadProfiles()
                                viewModel.loadAccountStats()
                            }
                        )
                    }
                }
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding() + 16.dp)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.errorMessage?.let { msg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = ErrorRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        uiState.successMessage?.let { msg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = SuccessGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        CashFlowTabContent(
                            uiState = uiState,
                            formatter = formatter,
                            commissionInput = commissionInput,
                            onCommissionInputChanged = { commissionInput = it },
                            deliveryFeeInput = deliveryFeeInput,
                            onDeliveryFeeInputChanged = { deliveryFeeInput = it },
                            freeDeliveryThresholdInput = freeDeliveryThresholdInput,
                            onFreeDeliveryThresholdInputChanged = { freeDeliveryThresholdInput = it },
                            onSaveSettings = {
                                val enteredCommission = commissionInput.toDoubleOrNull()
                                val enteredFee = deliveryFeeInput.toDoubleOrNull()
                                val enteredThreshold = freeDeliveryThresholdInput.toDoubleOrNull()
                                if (enteredCommission != null) {
                                    viewModel.updateSystemSetting("admin_commission_rate", enteredCommission / 100.0)
                                }
                                if (enteredFee != null) {
                                    viewModel.updateSystemSetting("base_delivery_fee", enteredFee)
                                }
                                if (enteredThreshold != null) {
                                    viewModel.updateSystemSetting("free_delivery_threshold", enteredThreshold)
                                }
                            },
                            onRefresh = {
                                viewModel.loadFinancialStats()
                                viewModel.loadDetailedFinancialStats()
                                viewModel.loadSystemSettings()
                            }
                        )
                    }
                }
                2 -> {
                    ProfileScreen(
                        onBack = { activeTab = 0 },
                        onNavigateToOrderHistory = {},
                        onNavigateToActivityHistory = {},
                        onLoggedOut = onLogout,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToChangePassword = onNavigateToChangePassword,
                        onNavigateToWallet = onNavigateToWallet,
                        onNavigateToPaymentSettings = onNavigateToPaymentSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun CashFlowTabContent(
    uiState: AdminUiState,
    formatter: NumberFormat,
    commissionInput: String,
    onCommissionInputChanged: (String) -> Unit,
    deliveryFeeInput: String,
    onDeliveryFeeInputChanged: (String) -> Unit,
    freeDeliveryThresholdInput: String,
    onFreeDeliveryThresholdInputChanged: (String) -> Unit,
    onSaveSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thống kê tiền chi tiết",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF673AB7).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFF673AB7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text("Tổng tiền hiện tại", fontSize = 13.sp)
                            }
                            Text(
                                text = formatter.format(uiState.detailedFinancialStats.totalSystemBalance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF673AB7)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9800).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text("Tiền chờ (Escrow)", fontSize = 13.sp)
                            }
                            Text(
                                text = formatter.format(uiState.detailedFinancialStats.pendingEscrowBalance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE91E63).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text("Tiền hoa hồng", fontSize = 13.sp)
                            }
                            Text(
                                text = formatter.format(uiState.detailedFinancialStats.totalCommissions),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE91E63)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBike,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text("Tiền cho shipper", fontSize = 13.sp)
                            }
                            Text(
                                text = formatter.format(uiState.detailedFinancialStats.totalShipperBalance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(InfoBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = InfoBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text("Tiền cho chủ quán", fontSize = 13.sp)
                            }
                            Text(
                                text = formatter.format(uiState.detailedFinancialStats.totalMerchantBalance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InfoBlue
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cấu hình hệ thống",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    OutlinedTextField(
                        value = commissionInput,
                        onValueChange = onCommissionInputChanged,
                        label = { Text("Tỷ lệ hoa hồng (%)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    OutlinedTextField(
                        value = deliveryFeeInput,
                        onValueChange = onDeliveryFeeInputChanged,
                        label = { Text("Phí giao hàng cơ bản (₫)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    OutlinedTextField(
                        value = freeDeliveryThresholdInput,
                        onValueChange = onFreeDeliveryThresholdInputChanged,
                        label = { Text("Ngưỡng miễn phí giao hàng (₫)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Button(
                        onClick = onSaveSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Cập nhật cấu hình", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Báo cáo theo ngày", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                }
            }
        }

        if (uiState.isFinancialLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }
        } else if (uiState.financialStats.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Chưa có báo cáo dòng tiền nào.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(uiState.financialStats) { stats ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stats.date.substringBefore(" "),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Badge(containerColor = OrangePrimary.copy(alpha = 0.1f)) {
                                Text(
                                    text = "${stats.totalOrders} đơn hàng",
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tổng Doanh Thu:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatter.format(stats.totalSubtotal), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Hoa Hồng Admin:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatter.format(stats.totalCommissions),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Giải Ngân Merchant:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatter.format(stats.totalMerchantPayouts), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Giải Ngân Shipper:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatter.format(stats.totalShipperPayouts), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountsTabContent(
    uiState: AdminUiState,
    formatter: NumberFormat,
    onSearchQueryChanged: (String) -> Unit,
    onRoleFilterChanged: (UserRole?) -> Unit,
    onSearchCriteriaChanged: (SearchCriteria) -> Unit,
    onEditBalanceRequested: (AdminProfileStats) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AccountPieChart(accountStats = uiState.accountStats)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Tìm theo tên, email, ID...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        cursorColor = OrangePrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                }
            }
        }

        item {
            var showRoleMenu by remember { mutableStateOf(false) }
            var showCriteriaMenu by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable { showRoleMenu = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (uiState.selectedRoleFilter) {
                                    null -> "Vai trò: Tất cả"
                                    UserRole.CUSTOMER -> "Vai trò: User"
                                    UserRole.MERCHANT -> "Vai trò: Store"
                                    UserRole.SHIPPER -> "Vai trò: Shipper"
                                    UserRole.ADMIN -> "Vai trò: Admin"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tất cả", fontSize = 13.sp) },
                            onClick = {
                                onRoleFilterChanged(null)
                                showRoleMenu = false
                            }
                        )
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (role) {
                                            UserRole.CUSTOMER -> "User"
                                            UserRole.MERCHANT -> "Store"
                                            UserRole.SHIPPER -> "Shipper"
                                            UserRole.ADMIN -> "Admin"
                                        },
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onRoleFilterChanged(role)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable { showCriteriaMenu = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (uiState.searchCriteria) {
                                    SearchCriteria.ALL -> "Tìm theo: Tất cả"
                                    SearchCriteria.NAME -> "Tìm theo: Tên"
                                    SearchCriteria.EMAIL -> "Tìm theo: Email"
                                    SearchCriteria.ID -> "Tìm theo: ID"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showCriteriaMenu,
                        onDismissRequest = { showCriteriaMenu = false }
                    ) {
                        SearchCriteria.entries.forEach { criteria ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (criteria) {
                                            SearchCriteria.ALL -> "Tất cả"
                                            SearchCriteria.NAME -> "Tên"
                                            SearchCriteria.EMAIL -> "Email"
                                            SearchCriteria.ID -> "ID"
                                        },
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onSearchCriteriaChanged(criteria)
                                    showCriteriaMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isAccountLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }
        } else if (uiState.filteredProfiles.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy tài khoản nào.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(uiState.filteredProfiles) { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditBalanceRequested(profile) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!profile.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            val iconColor = when (profile.role) {
                                UserRole.CUSTOMER -> InfoBlue
                                UserRole.MERCHANT -> AmberTertiary
                                UserRole.SHIPPER -> SuccessGreen
                                UserRole.ADMIN -> OrangePrimary
                            }
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (profile.role) {
                                        UserRole.CUSTOMER -> Icons.Default.Person
                                        UserRole.MERCHANT -> Icons.Default.Storefront
                                        UserRole.SHIPPER -> Icons.Default.DirectionsBike
                                        UserRole.ADMIN -> Icons.Default.SupervisorAccount
                                    },
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = profile.email,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatter.format(profile.balance),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = OrangePrimary
                            )
                            Text(
                                text = profile.role.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountPieChart(
    accountStats: List<AdminAccountStats>,
    modifier: Modifier = Modifier
) {
    val nonAdminStats = accountStats.filter { it.role != UserRole.ADMIN }
    val totalUsers = nonAdminStats.sumOf { it.totalUsers }
    val entries = nonAdminStats.filter { it.totalUsers > 0 }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 26.dp.toPx()
                val sizeMin = size.minDimension - strokeWidth
                val chartSize = Size(sizeMin, sizeMin)
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                
                if (totalUsers == 0L) {
                    drawArc(
                        color = Color.LightGray,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = chartSize,
                        style = Stroke(width = strokeWidth)
                    )
                } else {
                    var startAngle = -90f
                    entries.forEach { entry ->
                        val sweepAngle = (entry.totalUsers.toFloat() / totalUsers) * 360f
                        val color = when (entry.role) {
                            UserRole.CUSTOMER -> InfoBlue
                            UserRole.MERCHANT -> AmberTertiary
                            UserRole.SHIPPER -> SuccessGreen
                            UserRole.ADMIN -> OrangePrimary
                        }
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = chartSize,
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += sweepAngle
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalUsers.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tài khoản",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            UserRole.entries.filter { it != UserRole.ADMIN }.forEach { role ->
                val stats = accountStats.find { it.role == role }
                val count = stats?.totalUsers ?: 0
                val color = when (role) {
                    UserRole.CUSTOMER -> InfoBlue
                    UserRole.MERCHANT -> AmberTertiary
                    UserRole.SHIPPER -> SuccessGreen
                    UserRole.ADMIN -> OrangePrimary
                }
                val label = when (role) {
                    UserRole.CUSTOMER -> "User"
                    UserRole.MERCHANT -> "Store"
                    UserRole.SHIPPER -> "Shipper"
                    UserRole.ADMIN -> "Admin"
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = count.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
