@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.feature.customer_home.components.formatPrice

@Composable
fun ActivityHistoryScreen(
    onBack: () -> Unit,
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.activity_history_title), fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(start = 16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.errorResId?.let { resId ->
                Text(text = stringResource(resId), color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.items.isEmpty()) {
                Text(text = stringResource(R.string.activity_history_empty))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        val title = when (item.type) {
                            ActivityType.ORDER -> stringResource(
                                R.string.activity_history_order_title,
                                item.orderId ?: "-"
                            )
                            ActivityType.PAYMENT -> stringResource(
                                R.string.activity_history_payment_title,
                                item.paymentId ?: "-"
                            )
                        }
                        val subtitle = when (item.type) {
                            ActivityType.ORDER -> stringResource(
                                R.string.activity_history_item_subtitle,
                                item.status ?: "-",
                                item.totalPrice?.formatPrice() ?: "-"
                            )
                            ActivityType.PAYMENT -> stringResource(
                                R.string.activity_history_payment_subtitle,
                                item.orderId ?: "-",
                                resolvePaymentMethodLabel(item.method, item.provider),
                                item.status ?: "-"
                            )
                        }
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = subtitle)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.createdAt ?: stringResource(R.string.activity_history_time_unknown),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
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

@Composable
private fun resolvePaymentMethodLabel(
    method: PaymentMethod?,
    provider: WalletProvider?
): String {
    return when (method) {
        PaymentMethod.COD -> stringResource(R.string.payment_method_cod)
        PaymentMethod.CARD -> stringResource(R.string.payment_method_card)
        PaymentMethod.WALLET -> {
            val providerLabel = when (provider) {
                WalletProvider.ZALOPAY -> stringResource(R.string.payment_wallet_provider_zalopay)
                WalletProvider.MOMO -> stringResource(R.string.payment_wallet_provider_momo)
                WalletProvider.VNPAY -> stringResource(R.string.payment_wallet_provider_vnpay)
                WalletProvider.PAYPAL -> stringResource(R.string.payment_wallet_provider_paypal)
                WalletProvider.GOOGLE_PLAY -> stringResource(R.string.payment_wallet_provider_google)
                null -> stringResource(R.string.payment_method_wallet)
            }
            if (provider == null) {
                stringResource(R.string.payment_method_wallet)
            } else {
                stringResource(R.string.payment_wallet_method_with_provider, providerLabel)
            }
        }
        PaymentMethod.FOODIE_PAY -> stringResource(R.string.payment_method_foodie_pay)
        null -> "-"
    }
}

