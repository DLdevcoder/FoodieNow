@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.payment

import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.alpha
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Discount
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
    var showVoucherPickerDialog by remember { mutableStateOf(false) }
    var addressInitialized by remember { mutableStateOf(false) }
    var hasUserSelectedPaymentMethod by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.COD) }
    var selectedProvider by remember { mutableStateOf(WalletProvider.ZALOPAY) }

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
    val availablePaymentMethods = remember(uiState.configuredPaymentOptionIds) {
        PaymentMethod.entries.filter { method ->
            when (method) {
                PaymentMethod.COD,
                PaymentMethod.FOODIE_PAY -> true
                PaymentMethod.CARD -> PaymentMethodCatalog.CARD_ID in uiState.configuredPaymentOptionIds
                PaymentMethod.WALLET -> WalletProvider.entries.any { provider ->
                    PaymentMethodCatalog.optionIdFor(PaymentMethod.WALLET, provider) in uiState.configuredPaymentOptionIds
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
        uiState.defaultWalletProvider
    ) {
        if (uiState.paymentSettingsLoaded && !hasUserSelectedPaymentMethod) {
            selectedMethod = uiState.defaultPaymentMethod
            selectedProvider = uiState.defaultWalletProvider
        }
    }

    LaunchedEffect(uiState.configuredPaymentOptionIds, selectedMethod, selectedProvider) {
        val selectedOptionId = PaymentMethodCatalog.optionIdFor(
            method = selectedMethod,
            provider = if (selectedMethod == PaymentMethod.WALLET) selectedProvider else null
        )
        if (selectedOptionId !in uiState.configuredPaymentOptionIds) {
            selectedMethod = uiState.defaultPaymentMethod
            selectedProvider = uiState.defaultWalletProvider
        } else if (
            selectedMethod == PaymentMethod.WALLET &&
            selectedProvider !in availableWalletProviders
        ) {
            selectedProvider = availableWalletProviders.firstOrNull() ?: uiState.defaultWalletProvider
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
        useRewardPoints = useRewardPoints
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- ORDER SUMMARY ---
            Text(
                text = stringResource(R.string.payment_order_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (cartUiState.cartItems.isNotEmpty()) {
                        cartUiState.cartItems.forEach { (food, quantity) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${food.name} x$quantity")
                                Text(text = formatter.format(food.price * quantity))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(stringResource(R.string.payment_subtotal, formatter.format(subtotal)))
                    Text(stringResource(R.string.payment_delivery_fee, formatter.format(deliveryFee)))
                    if (totals.discountAmount > 0) {
                        Text(
                            stringResource(R.string.payment_voucher_discount, formatter.format(totals.discountAmount)),
                            color = com.example.foodienow.core.designsystem.theme.SuccessGreen
                        )
                    }
                    if (useRewardPoints && uiState.rewardPointsAvailable > 0) {
                        Text(
                            stringResource(R.string.payment_reward_discount, formatter.format(pointsDiscount)),
                            color = com.example.foodienow.core.designsystem.theme.WarningYellow
                        )
                    }
                    Text(
                        text = stringResource(R.string.payment_total, formatter.format(totalAmount)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // --- VOUCHER ---
            Text(
                text = stringResource(R.string.payment_voucher_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
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
                            label = { Text(stringResource(R.string.payment_voucher_hint)) },
                            singleLine = true
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
                                    appliedVoucherCode = code.takeIf { discount > 0L }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.payment_voucher_apply))
                        }
                    }

                    val potentialBestVoucher = uiState.availableVouchers
                        .filter { it.minOrderValue > subtotal && it.minOrderValue - subtotal <= 30000 }
                        .maxByOrNull { voucher ->
                            val raw = if (voucher.discountAmount > 0L) {
                                voucher.discountAmount
                            } else {
                                kotlin.math.floor(voucher.minOrderValue * voucher.discountPercent / 100.0).toLong()
                            }
                            if (voucher.maxDiscount > 0L) kotlin.math.min(raw, voucher.maxDiscount) else raw
                        }

                    potentialBestVoucher?.let { voucher ->
                        val needed = voucher.minOrderValue - subtotal
                        val raw = if (voucher.discountAmount > 0L) {
                            voucher.discountAmount
                        } else {
                            kotlin.math.floor(voucher.minOrderValue * voucher.discountPercent / 100.0).toLong()
                        }
                        val potentialDiscount = if (voucher.maxDiscount > 0L) kotlin.math.min(raw, voucher.maxDiscount) else raw

                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Discount,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Mua thêm ${formatter.format(needed)} để áp dụng mã ${voucher.code} (giảm ${formatter.format(potentialDiscount)})!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (uiState.availableVouchers.isNotEmpty()) {
                        TextButton(
                            onClick = { showVoucherPickerDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Chọn từ voucher khả dụng (${uiState.availableVouchers.size})",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // --- REWARD POINTS ---
            if (uiState.rewardPointsAvailable > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.payment_reward_title), fontWeight = FontWeight.Bold)
                            Text(
                            stringResource(R.string.payment_reward_detail, uiState.rewardPointsAvailable, formatter.format(uiState.rewardPointsAvailable.toLong())),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = useRewardPoints,
                            onCheckedChange = { useRewardPoints = it }
                        )
                    }
                }
            }

            // --- PAYMENT METHOD ---
            Text(
                text = stringResource(R.string.payment_method_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availablePaymentMethods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = {
                                selectedMethod = method
                                if (method == PaymentMethod.WALLET && selectedProvider !in availableWalletProviders) {
                                    selectedProvider = availableWalletProviders.firstOrNull() ?: WalletProvider.ZALOPAY
                                }
                                hasUserSelectedPaymentMethod = true
                            },
                            label = {
                                Text(
                                    text = when (method) {
                                        PaymentMethod.COD -> stringResource(R.string.payment_method_cod)
                                        PaymentMethod.CARD -> stringResource(R.string.payment_method_card)
                                        PaymentMethod.WALLET -> stringResource(R.string.payment_method_wallet)
                                        PaymentMethod.FOODIE_PAY -> stringResource(R.string.payment_method_foodie_pay)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // --- WALLET PROVIDER ---
            if (selectedMethod == PaymentMethod.WALLET) {
                Text(
                    text = stringResource(R.string.payment_wallet_provider_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableWalletProviders.forEach { provider ->
                            FilterChip(
                                selected = selectedProvider == provider,
                                onClick = {
                                    selectedProvider = provider
                                    hasUserSelectedPaymentMethod = true
                                },
                                label = {
                                    Text(
                                        text = when (provider) {
                                            WalletProvider.ZALOPAY -> stringResource(R.string.payment_wallet_provider_zalopay)
                                            WalletProvider.MOMO -> stringResource(R.string.payment_wallet_provider_momo)
                                            WalletProvider.VNPAY -> stringResource(R.string.payment_wallet_provider_vnpay)
                                            WalletProvider.PAYPAL -> stringResource(R.string.payment_wallet_provider_paypal)
                                            WalletProvider.GOOGLE_PLAY -> stringResource(R.string.payment_wallet_provider_google)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // --- DELIVERY INFO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = {
                            deliveryAddress = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.payment_delivery_address_label)) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = {
                            note = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.payment_note_label)) }
                    )
                }
            }

            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }

            uiState.infoMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = { showConfirmDialog = true },
                enabled = canPay,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.payment_processing))
                } else {
                    Text(stringResource(R.string.payment_confirm_button))
                }
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_back))
            }
        }
    }

    if (showConfirmDialog) {
        val methodLabel = when (selectedMethod) {
            PaymentMethod.COD -> stringResource(R.string.payment_method_cod)
            PaymentMethod.CARD -> stringResource(R.string.payment_method_card)
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
                        viewModel.submitPayment(
                            method = selectedMethod,
                            provider = if (selectedMethod == PaymentMethod.WALLET) selectedProvider else null,
                            deliveryAddress = deliveryAddress,
                            note = note,
                            amount = totalAmount,
                            usedRewardPoints = pointsDiscount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                            voucherCode = appliedVoucherCode
                        )
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
            title = { Text(text = "Chọn Voucher", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showVoucherPickerDialog = false }) {
                    Text(text = "Đóng")
                }
            }
        )
    }
}
