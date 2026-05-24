package com.example.foodienow.feature.shipper

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ShipperEarningsScreen(
    viewModel: ShipperEarningsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

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
            Column(
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
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thu nhập & Ví",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Số dư khả dụng",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatter.format(uiState.currentBalance)} ₫",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
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
