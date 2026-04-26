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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private enum class PaymentMethod(val label: String) {
    COD("Thanh toan khi nhan hang"),
    CARD("The ngan hang"),
    WALLET("Vi dien tu")
}

@Composable
fun PaymentScreen(
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.COD) }
    var deliveryAddress by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    val canPay = deliveryAddress.isNotBlank() && !isProcessing

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toan") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thong tin don hang",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Tong tam tinh: 120.000 VND")
            Text("Phi giao hang: 15.000 VND")
            Text(
                text = "Tong thanh toan: 135.000 VND",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Phuong thuc thanh toan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = { Text(method.label) }
                    )
                }
            }

            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = {
                    deliveryAddress = it
                    resultMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dia chi nhan hang") },
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    resultMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ghi chu (tuy chon)") }
            )

            resultMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    isProcessing = true
                    resultMessage = null
                },
                enabled = canPay,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dang xu ly")
                } else {
                    Text("Xac nhan thanh toan")
                }
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quay lai")
            }
        }
    }

    if (isProcessing) {
        // Fake processing state to keep the flow visible before integrating real payment API.
        androidx.compose.runtime.LaunchedEffect(Unit) {
            delay(1200)
            isProcessing = false
            resultMessage = "Thanh toan thanh cong. Don hang dang cho xac nhan."
        }
    }
}

