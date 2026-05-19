@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.payment

import androidx.compose.foundation.horizontalScroll
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
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.PaymentTotalsCalculator
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    onNavigateToPaymentResult: (orderId: String, amount: Long, methodLabel: String) -> Unit = { _, _, _ -> onNavigateToOrderHistory() },
    viewModel: PaymentViewModel = hiltViewModel()
) {
    var deliveryAddress by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var voucherCodeText by remember { mutableStateOf("") }
    var appliedVoucherCode by remember { mutableStateOf<String?>(null) }
    var discountAmount by remember { mutableStateOf(0L) }
    var useRewardPoints by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var addressInitialized by remember { mutableStateOf(false) }
    var paymentMethodInitialized by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.COD) }
    var selectedProvider by remember { mutableStateOf(WalletProvider.ZALOPAY) }

    val scope = rememberCoroutineScope()
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    val cartViewModel: com.example.foodienow.feature.cart.CartViewModel = hiltViewModel()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    if (!addressInitialized && uiState.defaultAddress.isNotBlank()) {
        deliveryAddress = uiState.defaultAddress
        addressInitialized = true
    }

    if (!paymentMethodInitialized && uiState.paymentSettingsLoaded) {
        selectedMethod = uiState.defaultPaymentMethod
        selectedProvider = uiState.defaultWalletProvider
        paymentMethodInitialized = true
    }

    val subtotal = cartUiState.cartItems.entries.sumOf { it.key.price * it.value }
    val cartStoreId = cartUiState.cartItems.keys.firstOrNull()?.storeId
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

    LaunchedEffect(Unit) {
        viewModel.paymentEvent.collect { event ->
            when (event) {
                is PaymentEvent.PaymentSuccess -> {
                    onNavigateToPaymentResult(event.orderId, event.amount, event.methodLabel)
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
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = voucherCodeText,
                        onValueChange = {
                            voucherCodeText = it
                            appliedVoucherCode = null
                            discountAmount = 0L
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
                    PaymentMethod.entries.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
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
                        WalletProvider.entries.forEach { provider ->
                            FilterChip(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
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
}
