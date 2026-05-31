@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.payment

import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import com.example.foodienow.BuildConfig
import com.example.foodienow.data.remote.GoongPrediction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.HorizontalDivider
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.core.designsystem.theme.OrangePrimary
import com.example.foodienow.core.designsystem.theme.FoodieCreamSurface
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.VoucherCard
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.PaymentMethodCatalog
import com.example.foodienow.domain.payment.PaymentTotalsCalculator
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    onNavigateToPaymentResult: (orderId: String, amount: Long, methodLabel: String) -> Unit = { _, _, _ -> onNavigateToOrderHistory() },
    onNavigateToLogin: () -> Unit = {},
    viewModel: PaymentViewModel = hiltViewModel()
) {
    var deliveryAddress by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var voucherCodeText by remember { mutableStateOf("") }
    var appliedVoucherCode by remember { mutableStateOf<String?>(null) }
    var discountAmount by remember { mutableStateOf(0L) }
    var useRewardPoints by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var activeBanner by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showVoucherPickerDialog by remember { mutableStateOf(false) }
    var addressInitialized by remember { mutableStateOf(false) }
    var hasUserSelectedPaymentMethod by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.COD) }
    var selectedProvider by remember { mutableStateOf(WalletProvider.ZALOPAY) }
    var showSavedAddressDialog by remember { mutableStateOf(false) }
    var showMapPickerDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    val cartViewModel: com.example.foodienow.feature.cart.CartViewModel = hiltViewModel()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val availableWalletProviders = remember(uiState.configuredPaymentOptionIds) {
        WalletProvider.entries.filter { provider ->
            PaymentMethodCatalog.optionIdFor(PaymentMethod.WALLET, provider) in uiState.configuredPaymentOptionIds
        }
    }
    val availablePaymentMethods = remember(uiState.configuredPaymentOptionIds, uiState.userRole) {
        PaymentMethod.entries.filter { method ->
            if (uiState.userRole == com.example.foodienow.domain.model.UserRole.ADMIN && method == PaymentMethod.COD) {
                false
            } else {
                when (method) {
                    PaymentMethod.COD,
                    PaymentMethod.FOODIE_PAY -> true
                    PaymentMethod.WALLET -> WalletProvider.entries.any { provider ->
                        PaymentMethodCatalog.optionIdFor(PaymentMethod.WALLET, provider) in uiState.configuredPaymentOptionIds
                    }
                }
            }
        }
    }

    if (!addressInitialized && uiState.defaultAddress.isNotBlank()) {
        deliveryAddress = uiState.defaultAddress
        addressInitialized = true
    }

    LaunchedEffect(
        uiState.paymentSettingsLoaded,
        uiState.defaultPaymentMethod,
        uiState.defaultWalletProvider,
        uiState.userRole
    ) {
        if (uiState.paymentSettingsLoaded && !hasUserSelectedPaymentMethod) {
            val defMethod = if (uiState.userRole == com.example.foodienow.domain.model.UserRole.ADMIN && uiState.defaultPaymentMethod == PaymentMethod.COD) {
                PaymentMethod.FOODIE_PAY
            } else {
                uiState.defaultPaymentMethod
            }
            selectedMethod = defMethod
            selectedProvider = uiState.defaultWalletProvider
        }
    }

    LaunchedEffect(uiState.configuredPaymentOptionIds, selectedMethod, selectedProvider, uiState.userRole) {
        var nextMethod = selectedMethod
        if (uiState.userRole == com.example.foodienow.domain.model.UserRole.ADMIN && nextMethod == PaymentMethod.COD) {
            nextMethod = PaymentMethod.FOODIE_PAY
        }
        val selectedOptionId = PaymentMethodCatalog.optionIdFor(
            method = nextMethod,
            provider = if (nextMethod == PaymentMethod.WALLET) selectedProvider else null
        )
        if (selectedOptionId !in uiState.configuredPaymentOptionIds) {
            val defMethod = if (uiState.userRole == com.example.foodienow.domain.model.UserRole.ADMIN && uiState.defaultPaymentMethod == PaymentMethod.COD) {
                PaymentMethod.FOODIE_PAY
            } else {
                uiState.defaultPaymentMethod
            }
            selectedMethod = defMethod
            selectedProvider = uiState.defaultWalletProvider
        } else {
            selectedMethod = nextMethod
            if (
                selectedMethod == PaymentMethod.WALLET &&
                selectedProvider !in availableWalletProviders
            ) {
                selectedProvider = availableWalletProviders.firstOrNull() ?: uiState.defaultWalletProvider
            }
        }
    }

    val subtotal = cartUiState.cartItems.entries.sumOf { it.key.price * it.value }
    val cartStoreId = cartUiState.cartItems.keys.firstOrNull()?.storeId

    LaunchedEffect(cartStoreId) {
        cartStoreId?.let { viewModel.loadAvailableVouchers(it, subtotal) }
    }

    LaunchedEffect(uiState.selectedVoucher) {
        val selected = uiState.selectedVoucher
        if (selected != null) {
            voucherCodeText = selected.code
            val discount = viewModel.applyVoucher(
                code = selected.code,
                storeId = cartStoreId,
                subtotal = subtotal
            )
            discountAmount = discount
            appliedVoucherCode = selected.code.takeIf { discount > 0L }
        }
    }
    val totals = PaymentTotalsCalculator.calculate(
        subtotal = subtotal,
        voucherDiscount = discountAmount,
        rewardPointsAvailable = uiState.rewardPointsAvailable,
        useRewardPoints = useRewardPoints,
        baseDeliveryFee = uiState.baseDeliveryFee,
        freeDeliveryThreshold = uiState.freeDeliveryThreshold
    )
    val deliveryFee = totals.deliveryFee
    val pointsDiscount = totals.pointsDiscount
    val totalAmount = totals.amountCharged

    val canPay = deliveryAddress.isNotBlank() && !uiState.isProcessing && cartUiState.cartItems.isNotEmpty()

    LaunchedEffect(deliveryAddress, note, selectedMethod) {
        viewModel.clearMessage()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.paymentEvent.collect { event ->
            when (event) {
                is PaymentEvent.PaymentSuccess -> {
                    if (event.methodLabel.contains("FoodiePay") || event.methodLabel.contains("Ví điện tử")) {
                        activeBanner = Pair("Thanh toán thành công", "Số dư Ví FoodiePay đã trừ -${formatter.format(event.amount)} cho đơn hàng ${event.orderId}.")
                    } else {
                        activeBanner = Pair("Đặt hàng thành công", "Đơn hàng ${event.orderId} thanh toán bằng Tiền mặt (COD). Số tiền: ${formatter.format(event.amount)}.")
                    }

                    kotlinx.coroutines.delay(2200)
                    onNavigateToPaymentResult(event.orderId, event.amount, event.methodLabel)
                }
                is PaymentEvent.SessionExpired -> {
                    android.widget.Toast.makeText(
                        context,
                        "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    onNavigateToLogin()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.payment_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FoodieCream,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = FoodieCream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.payment_order_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (cartUiState.cartItems.isNotEmpty()) {
                        cartUiState.cartItems.forEach { (food, quantity) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${food.name} x$quantity",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = formatter.format(food.price * quantity),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tạm tính:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatter.format(subtotal),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Phí giao hàng:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatter.format(deliveryFee),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (totals.discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Giảm giá voucher:",
                                fontSize = 14.sp,
                                color = SuccessGreen
                            )
                            Text(
                                text = "-${formatter.format(totals.discountAmount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                    if (useRewardPoints && uiState.rewardPointsAvailable > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Điểm thưởng sử dụng:",
                                fontSize = 14.sp,
                                color = Color(0xFFF25C54)
                            )
                            Text(
                                text = "-${formatter.format(pointsDiscount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF25C54)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tổng thanh toán:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatter.format(totalAmount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrangePrimary
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.payment_voucher_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showVoucherPickerDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(OrangePrimary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Discount,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (appliedVoucherCode == null) {
                            Column {
                                Text(
                                    text = "Chọn hoặc nhập mã giảm giá",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.availableVouchers.size.toString() + " mã giảm giá có thể sử dụng",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column {
                                Text(
                                    text = "Đã áp dụng: " + appliedVoucherCode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Giảm " + formatter.format(discountAmount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (appliedVoucherCode == null) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                voucherCodeText = ""
                                appliedVoucherCode = null
                                discountAmount = 0L
                                viewModel.selectVoucher(null)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Xóa voucher",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.rewardPointsAvailable > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFFB703).copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(stringResource(R.string.payment_reward_title), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    stringResource(R.string.payment_reward_detail, uiState.rewardPointsAvailable, formatter.format(uiState.rewardPointsAvailable.toLong())),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = useRewardPoints,
                            onCheckedChange = { useRewardPoints = it },
                            modifier = Modifier.scale(0.8f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OrangePrimary
                            )
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.payment_method_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        availablePaymentMethods.forEach { method ->
                            val isSelected = selectedMethod == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) OrangePrimary.copy(alpha = 0.08f) else FoodieCreamSurface)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        selectedMethod = method
                                        if (method == PaymentMethod.WALLET && selectedProvider !in availableWalletProviders) {
                                            selectedProvider = availableWalletProviders.firstOrNull() ?: WalletProvider.ZALOPAY
                                        }
                                        hasUserSelectedPaymentMethod = true
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val methodIcon = when (method) {
                                        PaymentMethod.COD -> Icons.Default.Money
                                        PaymentMethod.FOODIE_PAY -> Icons.Default.AccountBalanceWallet
                                        PaymentMethod.WALLET -> Icons.Default.CreditCard
                                    }
                                    Icon(
                                        imageVector = methodIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (method) {
                                            PaymentMethod.COD -> "Tiền mặt"
                                            PaymentMethod.WALLET -> "Ví điện tử"
                                            PaymentMethod.FOODIE_PAY -> "FoodiePay"
                                        },
                                        fontSize = 12.sp,
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

                    if (selectedMethod == PaymentMethod.WALLET) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
                        Text(
                            text = "Chọn ví điện tử:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableWalletProviders.forEach { provider ->
                                val isSelected = selectedProvider == provider
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) OrangePrimary.copy(alpha = 0.08f) else FoodieCreamSurface)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedProvider = provider
                                            hasUserSelectedPaymentMethod = true
                                        }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        val iconRes = when (provider) {
                                            WalletProvider.ZALOPAY -> R.drawable.ic_zalopay
                                            WalletProvider.MOMO -> R.drawable.ic_momo
                                            WalletProvider.VNPAY -> R.drawable.ic_vnpay
                                            WalletProvider.PAYPAL -> R.drawable.ic_paypal
                                        }
                                        Image(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = when (provider) {
                                                WalletProvider.ZALOPAY -> "ZaloPay"
                                                WalletProvider.MOMO -> "MoMo"
                                                WalletProvider.VNPAY -> "VNPAY"
                                                WalletProvider.PAYPAL -> "PayPal"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(12.dp)
                                                .background(OrangePrimary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Thông tin giao hàng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = {
                            deliveryAddress = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.payment_delivery_address_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            focusedLabelColor = OrangePrimary,
                            cursorColor = OrangePrimary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showSavedAddressDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FoodieCreamSurface, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp), tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Địa chỉ đã lưu", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showMapPickerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FoodieCreamSurface, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp), tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chọn từ bản đồ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = {
                            note = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.payment_note_label)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            focusedLabelColor = OrangePrimary,
                            cursorColor = OrangePrimary
                        )
                    )
                }
            }

            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            uiState.infoMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = { showConfirmDialog = true },
                enabled = canPay,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.payment_processing), fontWeight = FontWeight.Bold)
                } else {
                    Text(stringResource(R.string.payment_confirm_button), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }


        }
    }

    if (showConfirmDialog) {
        val methodLabel = when (selectedMethod) {
            PaymentMethod.COD -> stringResource(R.string.payment_method_cod)
            PaymentMethod.WALLET -> stringResource(R.string.payment_wallet_method_with_provider, selectedProvider.name)
            PaymentMethod.FOODIE_PAY -> stringResource(R.string.payment_method_foodie_pay)
        }
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.payment_confirm_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.payment_confirm_dialog_message))
                    Text(stringResource(R.string.payment_confirm_dialog_method, methodLabel), fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.payment_confirm_dialog_total, formatter.format(totalAmount)), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.payment_confirm_dialog_address, deliveryAddress))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        if (selectedMethod == PaymentMethod.FOODIE_PAY) {
                            showBiometricDialog = true
                        } else {
                            val lat = if (deliveryAddress == uiState.selectedAddress?.detail) uiState.selectedAddress?.latitude else null
                            val lng = if (deliveryAddress == uiState.selectedAddress?.detail) uiState.selectedAddress?.longitude else null
                            viewModel.submitPayment(
                                method = selectedMethod,
                                provider = if (selectedMethod == PaymentMethod.WALLET) selectedProvider else null,
                                deliveryAddress = deliveryAddress,
                                note = note,
                                amount = totalAmount,
                                usedRewardPoints = pointsDiscount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                                voucherCode = appliedVoucherCode,
                                deliveryLat = lat,
                                deliveryLng = lng
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.payment_confirm_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.payment_confirm_dialog_cancel))
                }
            }
        )
    }

    if (showVoucherPickerDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showVoucherPickerDialog = false },
            title = { Text(text = "Chọn hoặc nhập Voucher", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = voucherCodeText,
                            onValueChange = {
                                voucherCodeText = it
                                appliedVoucherCode = null
                                discountAmount = 0L
                                viewModel.selectVoucher(null)
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập mã voucher...") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                focusedLabelColor = OrangePrimary,
                                cursorColor = OrangePrimary
                            )
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    val code = voucherCodeText.trim()
                                    val discount = viewModel.applyVoucher(
                                        code = voucherCodeText,
                                        storeId = cartStoreId,
                                        subtotal = subtotal
                                    )
                                    discountAmount = discount
                                    if (discount > 0L) {
                                        appliedVoucherCode = code
                                        showVoucherPickerDialog = false
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Mã giảm giá không hợp lệ hoặc không áp dụng được.",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Áp dụng", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)

                    if (uiState.availableVouchers.isEmpty()) {
                        Text(
                            text = "Không tìm thấy voucher khả dụng nào cho cửa hàng này.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        uiState.availableVouchers.forEach { voucher ->
                            val isEligible = subtotal >= voucher.minOrderValue
                            val title = if (voucher.discountPercent > 0) {
                                "Giảm ${voucher.discountPercent}%"
                            } else {
                                "Giảm ${formatter.format(voucher.discountAmount)}"
                            }
                            val desc = buildString {
                                append("Đơn tối thiểu ${formatter.format(voucher.minOrderValue)}")
                                if (voucher.maxDiscount > 0) {
                                    append(". Giảm tối đa ${formatter.format(voucher.maxDiscount)}")
                                }
                                if (!isEligible) {
                                    append("\n⚠️ Chưa đủ điều kiện (Thiếu ${formatter.format(voucher.minOrderValue - subtotal)})")
                                }
                            }
                            val expiry = voucher.expiresAt?.let {
                                runCatching {
                                    val instant = java.time.Instant.parse(it)
                                    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
                                    val dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    "Hạn dùng: ${dtf.format(zoned)}"
                                }.getOrNull()
                            }

                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (isEligible) 1f else 0.5f)
                            ) {
                                VoucherCard(
                                    title = "${voucher.code} - $title",
                                    description = desc,
                                    expiryText = expiry,
                                    onClick = {
                                        if (isEligible) {
                                            showVoucherPickerDialog = false
                                            voucherCodeText = voucher.code
                                            viewModel.selectVoucher(voucher)
                                            scope.launch {
                                                val discount = viewModel.applyVoucher(
                                                    code = voucher.code,
                                                    storeId = cartStoreId,
                                                    subtotal = subtotal
                                                )
                                                discountAmount = discount
                                                appliedVoucherCode = voucher.code.takeIf { discount > 0L }
                                            }
                                        } else {
                                            val missing = voucher.minOrderValue - subtotal
                                            android.widget.Toast.makeText(
                                                context,
                                                "Chưa đủ điều kiện áp dụng voucher này (Thiếu ${formatter.format(missing)})",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showVoucherPickerDialog = false }) {
                    Text(text = "Đóng")
                }
            }
        )
    }

    if (uiState.checkoutUrl != null) {
        if (selectedMethod == PaymentMethod.WALLET) {
            var showWalletTransition by remember(uiState.checkoutUrl) { mutableStateOf(true) }
            LaunchedEffect(uiState.checkoutUrl) {
                if (uiState.checkoutUrl != null) {
                    showWalletTransition = true
                    kotlinx.coroutines.delay(1800)
                    showWalletTransition = false
                    viewModel.handleWebViewResult(true)
                }
            }

            if (showWalletTransition) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { viewModel.handleWebViewResult(false) },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    val brandColor = when (selectedProvider) {
                        WalletProvider.VNPAY -> Color(0xFF003087)
                        WalletProvider.MOMO -> Color(0xFFD82D8B)
                        WalletProvider.ZALOPAY -> Color(0xFF00C13F)
                        WalletProvider.PAYPAL -> Color(0xFF003087)
                    }
                    val providerName = when (selectedProvider) {
                        WalletProvider.VNPAY -> "VNPAY"
                        WalletProvider.MOMO -> "MoMo"
                        WalletProvider.ZALOPAY -> "ZaloPay"
                        WalletProvider.PAYPAL -> "PayPal"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FoodieCream)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = brandColor,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Đang xử lý thanh toán...",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = brandColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Đang kết nối ví và xử lý thanh toán an toàn qua cổng liên kết " + providerName + ". Vui lòng không đóng ứng dụng hoặc quay lại.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            CircularProgressIndicator(
                                color = brandColor,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }
        } else {
            var showTransition by remember(uiState.checkoutUrl) { mutableStateOf(true) }
            LaunchedEffect(uiState.checkoutUrl) {
                if (uiState.checkoutUrl != null) {
                    showTransition = true
                    kotlinx.coroutines.delay(1800)
                    showTransition = false
                }
            }

            androidx.compose.ui.window.Dialog(
                onDismissRequest = { viewModel.handleWebViewResult(false) },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    if (showTransition) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(FoodieCream)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Đang kết nối an toàn...",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Hệ thống đang thiết lập liên kết bảo mật đến cổng thanh toán ${selectedProvider.name} Sandbox. Vui lòng không đóng ứng dụng hoặc quay lại.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.handleWebViewResult(false) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                                Text("Thanh toán an toàn", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    android.webkit.WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.setSupportMultipleWindows(true)
                                        webViewClient = object : android.webkit.WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                                val url = request?.url?.toString() ?: return false
                                                if (url.startsWith("foodienow://payment_result")) {
                                                    viewModel.handleWebViewResult(true)
                                                    return true
                                                }
                                                return false
                                            }
                                        }
                                    }
                                },
                                update = { webView ->
                                    webView.loadUrl(uiState.checkoutUrl!!)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBiometricDialog) {
        var isScanning by remember { mutableStateOf(true) }
        var scanSuccess by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1800)
            isScanning = false
            scanSuccess = true
            kotlinx.coroutines.delay(800)
            showBiometricDialog = false

            val lat = if (deliveryAddress == uiState.selectedAddress?.detail) uiState.selectedAddress?.latitude else null
            val lng = if (deliveryAddress == uiState.selectedAddress?.detail) uiState.selectedAddress?.longitude else null
            viewModel.submitPayment(
                method = selectedMethod,
                provider = if (selectedMethod == PaymentMethod.WALLET) selectedProvider else null,
                deliveryAddress = deliveryAddress,
                note = note,
                amount = totalAmount,
                usedRewardPoints = pointsDiscount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                voucherCode = appliedVoucherCode,
                deliveryLat = lat,
                deliveryLng = lng
            )
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showBiometricDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FoodieCream)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Xác thực FoodiePay",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(OrangePrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = OrangePrimary,
                                modifier = Modifier.size(72.dp),
                                strokeWidth = 3.dp
                            )
                        }
                        Icon(
                            imageVector = if (scanSuccess) Icons.Default.Check else Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = if (scanSuccess) SuccessGreen else OrangePrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Text(
                        text = if (isScanning) "Đang xác thực vân tay/Face ID..." else "Xác thực thành công!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (scanSuccess) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Thanh toán an toàn số tiền ${formatter.format(totalAmount)} từ Ví điện tử FoodiePay của bạn.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
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
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                        Text(text = msg, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showSavedAddressDialog) {
        AlertDialog(
            onDismissRequest = { showSavedAddressDialog = false },
            title = { Text(text = "Chọn địa chỉ đã lưu", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.addresses.isEmpty()) {
                        Text(
                            text = "Bạn chưa lưu địa chỉ nào. Vui lòng thêm địa chỉ trong Cài đặt.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.addresses.forEach { address ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        deliveryAddress = address.detail
                                        viewModel.selectSavedAddress(address)
                                        showSavedAddressDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = FoodieCreamSurface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = address.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = address.detail,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSavedAddressDialog = false }) {
                    Text(text = "Đóng")
                }
            }
        )
    }

    if (showMapPickerDialog) {
        val predictions = uiState.predictions
        val selectedLat = uiState.selectedLat
        val selectedLng = uiState.selectedLng
        val selectedDetail = uiState.selectedDetail
        val isResolving = uiState.isResolving

        var searchQuery by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            if (deliveryAddress.isNotBlank()) {
                searchQuery = deliveryAddress
                viewModel.searchAddress(deliveryAddress)
            }
        }

        val hasLocation = selectedLat != null && selectedLng != null
        val dialogScrollState = rememberScrollState()

        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                showMapPickerDialog = false
                viewModel.clearAddressForm()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Chọn địa chỉ giao hàng",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            showMapPickerDialog = false
                            viewModel.clearAddressForm()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchAddress(it)
                        },
                        placeholder = { Text("Tìm kiếm địa điểm...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.searchAddress("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(dialogScrollState)
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isResolving) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = OrangePrimary)
                                }
                            } else if (hasLocation) {
                                Text(
                                    text = selectedDetail,
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                AddMapPreview(
                                    latitude = selectedLat!!,
                                    longitude = selectedLng!!,
                                    onLocationChanged = { lat, lng ->
                                        viewModel.updateLocation(lat, lng)
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        deliveryAddress = selectedDetail
                                        showMapPickerDialog = false
                                        viewModel.clearAddressForm()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) {
                                    Text("Xác nhận địa chỉ giao hàng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = Color.LightGray
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Tìm kiếm địa chỉ để định vị trên bản đồ",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = predictions.isNotEmpty(),
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(predictions) { prediction ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    searchQuery = prediction.description
                                                    viewModel.selectPrediction(prediction)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = prediction.description,
                                                fontSize = 13.sp,
                                                maxLines = 2,
                                                color = Color(0xFF374151)
                                            )
                                        }
                                        if (prediction != predictions.last()) {
                                            HorizontalDivider(
                                                color = Color(0xFFE5E7EB),
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NativeWalletPaymentDialog(
    provider: WalletProvider,
    amount: Long,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    var cardError by remember { mutableStateOf<String?>(null) }
    var countdown by remember { mutableStateOf(60) }

    var momoPhone by remember { mutableStateOf("") }
    var momoPin by remember { mutableStateOf("") }
    var momoError by remember { mutableStateOf<String?>(null) }

    var zaloPin by remember { mutableStateOf("") }
    var zaloError by remember { mutableStateOf<String?>(null) }

    var paypalEmail by remember { mutableStateOf("") }
    var paypalPassword by remember { mutableStateOf("") }
    var paypalError by remember { mutableStateOf<String?>(null) }

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    LaunchedEffect(step) {
        if (step == 2) {
            kotlinx.coroutines.delay(1800)
            step = 3
        } else if (step == 3) {
            countdown = 60
            while (countdown > 0) {
                kotlinx.coroutines.delay(1000)
                countdown--
            }
        } else if (step == 4) {
            kotlinx.coroutines.delay(1500)
            onSuccess()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onCancel,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val brandColor = when (provider) {
            WalletProvider.VNPAY -> MaterialTheme.colorScheme.primary
            WalletProvider.MOMO -> Color(0xFFD82D8B)
            WalletProvider.ZALOPAY -> Color(0xFF00C13F)
            WalletProvider.PAYPAL -> Color(0xFF003087)
        }
        val headerTitle = when (provider) {
            WalletProvider.VNPAY -> "Cổng thanh toán NCB - VNPAY"
            WalletProvider.MOMO -> "Cổng thanh toán MoMo Sandbox"
            WalletProvider.ZALOPAY -> "Cổng thanh toán ZaloPay Sandbox"
            WalletProvider.PAYPAL -> "PayPal Sandbox Checkout"
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FoodieCream)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brandColor)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = headerTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (provider) {
                        WalletProvider.VNPAY -> {
                            if (step == 1) {
                                Text(
                                    text = "Thanh toán đơn hàng: ${formatter.format(amount)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandColor
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF003049), Color(0xFF669BBC), Color(0xFF003049))
                                            ),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "NCB ATM",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 20.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp, 28.dp)
                                                    .background(Color(0xFFE0A96D), RoundedCornerShape(6.dp))
                                            )
                                        }

                                        Text(
                                            text = if (cardNumber.isEmpty()) "•••• •••• •••• ••••" else cardNumber.chunked(4).joinToString(" "),
                                            color = Color.White,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.5.sp
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Column {
                                                Text(
                                                    text = "CHỦ THẺ",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 9.sp
                                                )
                                                Text(
                                                    text = if (cardName.isEmpty()) "NGUYEN VAN A" else cardName.uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "NGÀY PHÁT HÀNH",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 9.sp
                                                )
                                                Text(
                                                    text = if (cardExpiry.isEmpty()) "MM/YY" else cardExpiry,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        cardNumber = "9704198526191432198"
                                        cardName = "NGUYEN VAN A"
                                        cardExpiry = "07/15"
                                        cardError = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tự động điền thẻ Sandbox NCB", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { input: String ->
                                        if (input.all { it.isDigit() } && input.length <= 19) {
                                            cardNumber = input
                                            cardError = null
                                        }
                                    },
                                    label = { Text("Số thẻ") },
                                    placeholder = { Text("Ví dụ: 9704198526191432198") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = cardName,
                                    onValueChange = { input: String ->
                                        if (input.all { it.isLetter() || it.isWhitespace() }) {
                                            cardName = input
                                            cardError = null
                                        }
                                    },
                                    label = { Text("Tên chủ thẻ (không dấu)") },
                                    placeholder = { Text("Ví dụ: NGUYEN VAN A") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { input: String ->
                                        if (input.length <= 5) {
                                            cardExpiry = input
                                            cardError = null
                                        }
                                    },
                                    label = { Text("Ngày phát hành (MM/YY)") },
                                    placeholder = { Text("Ví dụ: 07/15") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                cardError?.let { err ->
                                    Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        if (cardNumber == "9704198526191432198" && cardName.trim().uppercase() == "NGUYEN VAN A" && cardExpiry.trim() == "07/15") {
                                            step = 2
                                        } else {
                                            cardError = "Thông tin thẻ ATM Sandbox chưa chính xác. Vui lòng kiểm tra lại hoặc sử dụng nút Tự động điền thẻ Test."
                                        }
                                    },
                                    enabled = cardNumber.isNotEmpty() && cardName.isNotEmpty() && cardExpiry.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Xác thực thẻ", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        WalletProvider.MOMO -> {
                            if (step == 1) {
                                Text(
                                    text = "Thanh toán qua Ví MoMo Sandbox: ${formatter.format(amount)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandColor,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        momoPhone = "0987654321"
                                        momoPin = "123456"
                                        momoError = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = brandColor.copy(alpha = 0.15f), contentColor = brandColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Tự động điền tài khoản MoMo Test", fontWeight = FontWeight.Bold)
                                }

                                OutlinedTextField(
                                    value = momoPhone,
                                    onValueChange = { if (it.length <= 11) momoPhone = it },
                                    label = { Text("Số điện thoại MoMo") },
                                    placeholder = { Text("Ví dụ: 0987654321") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandColor, focusedLabelColor = brandColor, cursorColor = brandColor)
                                )

                                OutlinedTextField(
                                    value = momoPin,
                                    onValueChange = { if (it.all { it.isDigit() } && it.length <= 6) momoPin = it },
                                    label = { Text("Mã PIN (6 chữ số)") },
                                    placeholder = { Text("Nhập mã bí mật") },
                                    singleLine = true,
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandColor, focusedLabelColor = brandColor, cursorColor = brandColor)
                                )

                                momoError?.let { err ->
                                    Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        if (momoPhone == "0987654321" && momoPin == "123456") {
                                            step = 2
                                        } else {
                                            momoError = "Số điện thoại hoặc mã PIN Test chưa chính xác. Vui lòng sử dụng thông tin tự động điền."
                                        }
                                    },
                                    enabled = momoPhone.isNotEmpty() && momoPin.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = brandColor)
                                ) {
                                    Text("Đăng nhập và Thanh toán", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        WalletProvider.ZALOPAY -> {
                            if (step == 1) {
                                Text(
                                    text = "Thanh toán qua Ví ZaloPay Sandbox: ${formatter.format(amount)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandColor,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(brandColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .border(1.dp, brandColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "Đơn hàng: Thanh toán FoodieNow", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "Tài khoản ZaloPay: foodienow_user", fontSize = 14.sp, color = Color.Gray)
                                        Text(text = "Số tiền: ${formatter.format(amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = brandColor)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        zaloPin = "123456"
                                        zaloError = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = brandColor.copy(alpha = 0.15f), contentColor = brandColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Tự động điền mã PIN thanh toán ZaloPay", fontWeight = FontWeight.Bold)
                                }

                                OutlinedTextField(
                                    value = zaloPin,
                                    onValueChange = { if (it.all { it.isDigit() } && it.length <= 6) zaloPin = it },
                                    label = { Text("Mã PIN thanh toán (6 chữ số)") },
                                    placeholder = { Text("Nhập 123456") },
                                    singleLine = true,
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandColor, focusedLabelColor = brandColor, cursorColor = brandColor)
                                )

                                zaloError?.let { err ->
                                    Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        if (zaloPin == "123456") {
                                            step = 2
                                        } else {
                                            zaloError = "Mã PIN thanh toán Test chưa chính xác. Vui lòng nhập 123456."
                                        }
                                    },
                                    enabled = zaloPin.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = brandColor)
                                ) {
                                    Text("Xác nhận thanh toán ZaloPay", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        WalletProvider.PAYPAL -> {
                            if (step == 1) {
                                Text(
                                    text = "PayPal Sandbox Checkout: ${formatter.format(amount)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandColor,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        paypalEmail = "foodienow.buyer@sandbox.paypal.com"
                                        paypalPassword = "paypal_secret_pass"
                                        paypalError = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = brandColor.copy(alpha = 0.15f), contentColor = brandColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Tự động điền tài khoản PayPal Sandbox", fontWeight = FontWeight.Bold)
                                }

                                OutlinedTextField(
                                    value = paypalEmail,
                                    onValueChange = { paypalEmail = it },
                                    label = { Text("Email PayPal Sandbox") },
                                    placeholder = { Text("buyer@sandbox.paypal.com") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandColor, focusedLabelColor = brandColor, cursorColor = brandColor)
                                )

                                OutlinedTextField(
                                    value = paypalPassword,
                                    onValueChange = { paypalPassword = it },
                                    label = { Text("Password") },
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandColor, focusedLabelColor = brandColor, cursorColor = brandColor)
                                )

                                paypalError?.let { err ->
                                    Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        if (paypalEmail == "foodienow.buyer@sandbox.paypal.com" && paypalPassword == "paypal_secret_pass") {
                                            step = 2
                                        } else {
                                            paypalError = "Tài khoản PayPal Sandbox chưa chính xác. Vui lòng dùng nút Tự động điền."
                                        }
                                    },
                                    enabled = paypalEmail.isNotEmpty() && paypalPassword.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = brandColor)
                                ) {
                                    Text("Log In & Continue", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    if (step == 2) {
                        Spacer(modifier = Modifier.height(40.dp))
                        CircularProgressIndicator(modifier = Modifier.size(56.dp), color = brandColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đang kết nối bảo mật đến cổng thanh toán...",
                            fontWeight = FontWeight.Bold,
                            color = brandColor
                        )
                        Text(
                            text = "Vui lòng không đóng ứng dụng.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (step == 3) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Xác thực mã OTP",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandColor
                        )

                        Text(
                            text = "Mã xác thực OTP đã được gửi đến số điện thoại liên kết. Vui lòng nhập mã để hoàn tất giao dịch thanh toán số tiền ${formatter.format(amount)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { input: String ->
                                if (input.all { it.isDigit() } && input.length <= 6) {
                                    otpCode = input
                                    otpError = null
                                }
                            },
                            label = { Text("Mã OTP") },
                            placeholder = { Text("Nhập 6 chữ số") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brandColor, focusedLabelColor = brandColor, cursorColor = brandColor)
                        )

                        otpError?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                otpCode = "123456"
                                otpError = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tự động điền OTP Sandbox (123456)", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (otpCode == "123456") {
                                    step = 4
                                } else {
                                    otpError = "Mã OTP chưa chính xác. Vui lòng nhập 123456 để thử nghiệm Sandbox."
                                }
                            },
                            enabled = otpCode.length >= 4,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandColor)
                        ) {
                            Text("Xác nhận thanh toán", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        TextButton(
                            onClick = {
                                countdown = 60
                            },
                            enabled = countdown == 0
                        ) {
                            Text(
                                text = if (countdown > 0) "Gửi lại mã sau ${countdown}s" else "Gửi lại mã OTP",
                                color = brandColor
                            )
                        }
                    }

                    if (step == 4) {
                        Spacer(modifier = Modifier.height(40.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "Thanh toán thành công!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "Đang chuyển hướng bạn quay về ứng dụng...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMapPreview(
    latitude: Double,
    longitude: Double,
    onLocationChanged: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapLibre.getInstance(context); MapView(context) }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var isReady by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(android.os.Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isReady, latitude, longitude) {
        if (!isReady) return@LaunchedEffect
        val map = mapInstance ?: return@LaunchedEffect
        val center = map.cameraPosition?.target
        if (center == null || (Math.abs(center.latitude - latitude) + Math.abs(center.longitude - longitude) > 0.0001)) {
            val pos = LatLng(latitude, longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { map ->
                    if (mapInstance == null) {
                        mapInstance = map
                        var isUserMovingMap = false

                        map.addOnCameraMoveStartedListener { reason ->
                            if (reason == 1) {
                                isUserMovingMap = true
                            }
                        }

                        map.setStyle("https://tiles.goong.io/assets/goong_map_web.json?api_key=${BuildConfig.GOONG_MAPTILES_KEY}") {
                            isReady = true
                        }

                        map.addOnCameraIdleListener {
                            if (isUserMovingMap) {
                                val target = map.cameraPosition?.target ?: return@addOnCameraIdleListener
                                onLocationChanged(target.latitude, target.longitude)
                                isUserMovingMap = false
                            }
                        }
                    }
                }
            }
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .offset(y = (-20).dp)
            )
        }
    }
}

