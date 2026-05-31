package com.example.foodienow.feature.shipper

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.core.designsystem.theme.OrangePrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ShipperEarningsScreen(
    onNavigateToPaymentSettings: () -> Unit,
    viewModel: ShipperEarningsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

    var showNotLinkedDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var selectedWallet by remember { mutableStateOf("") }
    var withdrawAmountText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(FoodieCream), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FoodieCream)
        ) {
            Surface(
                color = Color.Transparent,
                contentColor = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PromoGradientStart,
                                    MaterialTheme.colorScheme.primary,
                                    PromoGradientEnd
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(27.dp),
                                tint = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Thu nhập & Ví",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Số dư: ${formatter.format(uiState.currentBalance)} ₫",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.84f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                if (uiState.linkedWallets.isEmpty()) {
                                    showNotLinkedDialog = true
                                } else {
                                    selectedWallet = uiState.linkedWallets.first()
                                    withdrawAmountText = uiState.currentBalance.toString()
                                    amountError = null
                                    showWithdrawDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rút tiền",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Thống kê thu nhập",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FoodieCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Hôm nay",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatter.format(uiState.todayEarnings)} ₫",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tuần này",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatter.format(uiState.weekEarnings)} ₫",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Lịch sử giao dịch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (uiState.recentTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có giao dịch nào",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    FoodieCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(uiState.recentTransactions) { index, transaction ->
                                TransactionItem(transaction)
                                if (index < uiState.recentTransactions.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotLinkedDialog) {
        AlertDialog(
            onDismissRequest = { showNotLinkedDialog = false },
            title = { Text("Chưa liên kết ví rút tiền", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn cần liên kết ít nhất một tài khoản ví điện tử (MoMo, ZaloPay, VNPAY, PayPal) để có thể thực hiện rút tiền.") },
            confirmButton = {
                Button(
                    onClick = {
                        showNotLinkedDialog = false
                        onNavigateToPaymentSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Liên kết ngay", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNotLinkedDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Để sau", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Rút tiền về ví liên kết", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Số dư khả dụng: ${formatter.format(uiState.currentBalance)} ₫",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )

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
                            val isSelected = selectedWallet == walletId
                            OutlinedButton(
                                onClick = { selectedWallet = walletId },
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
                                amountError = null
                            }
                        },
                        label = { Text("Số tiền rút (₫)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = amountError != null
                    )

                    amountError?.let { err ->
                        Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val amount = withdrawAmountText.toLongOrNull() ?: 0L
                            if (amount <= 0) {
                                amountError = "Vui lòng nhập số tiền rút hợp lệ."
                            } else if (amount > uiState.currentBalance) {
                                amountError = "Số dư không đủ để thực hiện rút tiền."
                            } else if (amount < 10000L) {
                                amountError = "Số tiền rút tối thiểu là 10.000 ₫."
                            } else {
                                viewModel.withdraw(amount, selectedWallet)
                                showWithdrawDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isWithdrawing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Rút tiền", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showWithdrawDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Hủy", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.withdrawSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.resetWithdrawSuccess() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Text("Rút tiền thành công", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Giao dịch đã được thực hiện thành công.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Số tiền: ${formatter.format(uiState.lastWithdrawalAmount)} ₫",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Nhận bởi: Ví ${uiState.lastWithdrawalWallet}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Số dư hiện tại: ${formatter.format(uiState.currentBalance)} ₫",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetWithdrawSuccess() },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xác nhận", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (transaction.isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (transaction.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = if (transaction.isIncome) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateFormatter.format(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${if (transaction.isIncome) "+" else "-"}${formatter.format(transaction.amount)} ₫",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            color = if (transaction.isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}
