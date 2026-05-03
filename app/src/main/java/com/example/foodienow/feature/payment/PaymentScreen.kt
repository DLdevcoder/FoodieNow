@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.payment

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.COD) }
    var selectedProvider by remember { mutableStateOf(WalletProvider.ZALOPAY) }
    var deliveryAddress by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    var voucherCodeText by remember { mutableStateOf("") }
    var discountAmount by remember { mutableStateOf(0.0) }
    var useRewardPoints by remember { mutableStateOf(false) }

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    val cartViewModel: com.example.foodienow.feature.cart.CartViewModel = hiltViewModel()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val subtotal = cartUiState.cartItems.entries.sumOf { it.key.price * it.value }
    val deliveryFee = if (subtotal > 100000) 0.0 else 15000.0
    val pointsDiscount = if (useRewardPoints) uiState.rewardPointsAvailable.toDouble() else 0.0
    val totalAmount = maxOf(0.0, subtotal + deliveryFee - discountAmount - pointsDiscount)



    val canPay = deliveryAddress.isNotBlank() && !uiState.isProcessing && totalAmount > 0

    LaunchedEffect(deliveryAddress, note, selectedMethod) {
        viewModel.clearMessage()
    }

    LaunchedEffect(Unit) {
        viewModel.paymentEvent.collect { event ->
            when (event) {
                is PaymentEvent.PaymentSuccess -> {
                    onNavigateToOrderHistory()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.payment_title), fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
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
                fontWeight = FontWeight.SemiBold
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    if (discountAmount > 0) {
                        Text("Giảm giá (Voucher): -${formatter.format(discountAmount)}", color = Color(0xFF10B981))
                    }
                    if (useRewardPoints && uiState.rewardPointsAvailable > 0) {
                        Text("Điểm thưởng (FoodieCoins): -${formatter.format(pointsDiscount)}", color = Color(0xFFF59E0B))
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
                text = "Mã giảm giá",
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
                        onValueChange = { voucherCodeText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nhập mã (VD: GIAM20K)") },
                        singleLine = true
                    )
                    Button(
                        onClick = { discountAmount = viewModel.applyVoucher(voucherCodeText) }
                    ) {
                        Text("Áp dụng")
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
                            Text("Dùng FoodieCoins", fontWeight = FontWeight.Bold)
                            Text(
                                "Bạn có ${uiState.rewardPointsAvailable} điểm (giảm ${formatter.format(uiState.rewardPointsAvailable.toDouble())})",
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
                onClick = {
                    viewModel.submitPayment(
                        method = selectedMethod,
                        provider = if (selectedMethod == PaymentMethod.WALLET) selectedProvider else null,
                        deliveryAddress = deliveryAddress,
                        note = note,
                        amount = totalAmount,
                        usedRewardPoints = if (useRewardPoints) uiState.rewardPointsAvailable else 0
                    )
                },
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
}


