package com.example.foodienow.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieCreamSurface
import com.example.foodienow.core.designsystem.theme.OrangePrimary
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.core.designsystem.theme.ErrorRed
import com.example.foodienow.domain.payment.PaymentMethodCatalog
import com.example.foodienow.domain.model.UserRole

data class PaymentSettingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val requiresSetup: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSettingsScreen(
    onBack: () -> Unit,
    viewModel: PaymentSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var setupTarget by remember { mutableStateOf<PaymentSettingItem?>(null) }
    var removeTarget by remember { mutableStateOf<PaymentSettingItem?>(null) }

    val paymentMethods = remember(uiState.userRole) {
        val all = listOf(
            PaymentSettingItem(
                id = PaymentMethodCatalog.COD_ID,
                title = "Thanh toán tiền mặt",
                subtitle = "Thanh toán khi nhận hàng",
                icon = Icons.Default.Money,
                requiresSetup = false
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.FOODIE_PAY_ID,
                title = "Ví FoodiePay",
                subtitle = "Dùng số dư tài khoản FoodiePay",
                icon = Icons.Default.AccountBalanceWallet,
                requiresSetup = false
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.MOMO_ID,
                title = "Ví MoMo",
                subtitle = "Liên kết số điện thoại ví MoMo",
                iconRes = R.drawable.ic_momo,
                requiresSetup = true
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.ZALOPAY_ID,
                title = "ZaloPay",
                subtitle = "Liên kết số điện thoại ví ZaloPay",
                iconRes = R.drawable.ic_zalopay,
                requiresSetup = true
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.VNPAY_ID,
                title = "VNPAY",
                subtitle = "Liên kết tài khoản thẻ NCB Sandbox",
                iconRes = R.drawable.ic_vnpay,
                requiresSetup = true
            ),
            PaymentSettingItem(
                id = PaymentMethodCatalog.PAYPAL_ID,
                title = "PayPal",
                subtitle = "Liên kết địa chỉ email PayPal Sandbox",
                iconRes = R.drawable.ic_paypal,
                requiresSetup = true
            )
        )
        if (uiState.userRole == UserRole.ADMIN) {
            all.filter { it.id != PaymentMethodCatalog.COD_ID }
        } else {
            all
        }
    }

    var defaultMethodId = PaymentSettingsSelectionMapper.toOptionId(
        method = settings.defaultMethod,
        provider = settings.defaultProvider
    )
    if (uiState.userRole == UserRole.ADMIN && defaultMethodId == PaymentMethodCatalog.COD_ID) {
        defaultMethodId = PaymentMethodCatalog.FOODIE_PAY_ID
    }

    Scaffold(
        containerColor = FoodieCream,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Phương thức thanh toán", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FoodieCream,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            uiState.errorMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            uiState.infoMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message,
                        color = SuccessGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(paymentMethods, key = { it.id }) { method ->
                    val info = settings.methodInfos[method.id]
                    val isAvailable = settings.isOptionAvailable(method.id)
                    val isDefault = defaultMethodId == method.id

                    PaymentSettingCard(
                        method = method,
                        details = info?.details,
                        isAvailable = isAvailable,
                        isDefault = isDefault,
                        isSaving = uiState.isSaving,
                        onSetDefault = {
                            viewModel.clearMessage()
                            if (isAvailable) {
                                viewModel.updateDefaultMethod(method.id)
                            } else {
                                setupTarget = method
                            }
                        },
                        onSetup = {
                            viewModel.clearMessage()
                            setupTarget = method
                        },
                        onRemove = {
                            viewModel.clearMessage()
                            removeTarget = method
                        }
                    )
                }
            }
        }
    }

    setupTarget?.let { method ->
        SetupPaymentMethodDialog(
            method = method,
            isSaving = uiState.isSaving,
            onDismiss = { setupTarget = null },
            onSave = { details ->
                viewModel.savePaymentMethodInfo(
                    optionId = method.id,
                    displayName = method.title,
                    details = details
                )
                setupTarget = null
            }
        )
    }

    removeTarget?.let { method ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("Gỡ thông tin thanh toán", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Bạn chắc chắn muốn hủy liên kết ${method.title}? Nếu đây là phương thức thanh toán mặc định, hệ thống sẽ chuyển về tiền mặt."
                )
            },
            confirmButton = {
                Button(
                    enabled = !uiState.isSaving,
                    onClick = {
                        viewModel.removePaymentMethodInfo(method.id)
                        removeTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Gỡ bỏ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { removeTarget = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Hủy", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PaymentSettingCard(
    method: PaymentSettingItem,
    details: String?,
    isAvailable: Boolean,
    isDefault: Boolean,
    isSaving: Boolean,
    onSetDefault: () -> Unit,
    onSetup: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isDefault) OrangePrimary else Color.Transparent
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDefault) FoodieCreamSurface else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (method.iconRes != null) {
                        Image(
                            painter = painterResource(id = method.iconRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (method.icon != null) {
                        Icon(
                            imageVector = method.icon,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = method.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isDefault) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = SuccessGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Mặc định",
                                    color = SuccessGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = details ?: method.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    if (method.requiresSetup && !isAvailable) {
                        Text(
                            text = "Chưa liên kết tài khoản",
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isDefault) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (method.requiresSetup && !isAvailable) {
                    Button(
                        enabled = !isSaving,
                        onClick = onSetup,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Liên kết ngay", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    if (!isDefault) {
                        Button(
                            enabled = !isSaving,
                            onClick = onSetDefault,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Đặt mặc định", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    if (method.requiresSetup) {
                        OutlinedButton(
                            enabled = !isSaving,
                            onClick = onRemove,
                            border = BorderStroke(1.dp, ErrorRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = if (!isDefault) Modifier.weight(1f).height(38.dp) else Modifier.height(38.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hủy liên kết", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = OrangePrimary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupPaymentMethodDialog(
    method: PaymentSettingItem,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    
    var vnpCardNumber by remember { mutableStateOf("") }
    var vnpCardName by remember { mutableStateOf("") }
    var vnpCardExpiry by remember { mutableStateOf("") }
    var vnpCardError by remember { mutableStateOf<String?>(null) }

    var paypalEmail by remember { mutableStateOf("") }
    var paypalPassword by remember { mutableStateOf("") }
    var paypalError by remember { mutableStateOf<String?>(null) }
    var showPaypalLoading by remember { mutableStateOf(false) }

    LaunchedEffect(showPaypalLoading) {
        if (showPaypalLoading) {
            kotlinx.coroutines.delay(1500)
            onSave("PayPal - ${paypalEmail.trim()}")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (method.id) {
                    PaymentMethodCatalog.MOMO_ID, PaymentMethodCatalog.ZALOPAY_ID -> {
                        if (step == 1) "Liên kết ví ${method.title}" else "Xác thực OTP liên kết"
                    }
                    PaymentMethodCatalog.VNPAY_ID -> {
                        if (step == 1) "Liên kết thẻ nội địa VNPAY" else "Xác thực OTP Ngân hàng"
                    }
                    PaymentMethodCatalog.PAYPAL_ID -> "Liên kết tài khoản PayPal"
                    else -> "Liên kết phương thức thanh toán"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (method.id == PaymentMethodCatalog.MOMO_ID || method.id == PaymentMethodCatalog.ZALOPAY_ID) {
                    if (step == 1) {
                        Text(
                            text = "Nhập số điện thoại đăng ký tài khoản ví điện tử ${method.title} của bạn để gửi yêu cầu liên kết.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input: String ->
                                if (input.all { it.isDigit() } && input.length <= 10) {
                                    phoneNumber = input
                                }
                            },
                            label = { Text("Số điện thoại liên kết") },
                            placeholder = { Text("Ví dụ: 0987654321") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { phoneNumber = "0987654321" },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tự động điền số điện thoại Test", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Mã OTP xác thực liên kết đã được gửi tới số điện thoại ${phoneNumber}. Vui lòng nhập mã để hoàn tất ví ${method.title}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { input: String ->
                                if (input.all { it.isDigit() } && input.length <= 6) {
                                    otpCode = input
                                    otpError = null
                                }
                            },
                            label = { Text("Mã xác thực OTP") },
                            placeholder = { Text("Nhập 6 chữ số") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        )

                        otpError?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                otpCode = "123456"
                                otpError = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tự động điền OTP Sandbox (123456)", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (method.id == PaymentMethodCatalog.VNPAY_ID) {
                    if (step == 1) {
                        Text(
                            text = "Cung cấp thông tin thẻ ATM ngân hàng NCB Sandbox để thực hiện quy trình liên kết tài khoản ngân hàng thực tế.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = "NCB (Ngân hàng Quốc Dân)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ngân hàng") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = vnpCardNumber,
                            onValueChange = { input: String ->
                                if (input.all { it.isDigit() } && input.length <= 19) {
                                    vnpCardNumber = input
                                    vnpCardError = null
                                }
                            },
                            label = { Text("Số thẻ ATM") },
                            placeholder = { Text("Ví dụ: 9704198526191432198") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = vnpCardName,
                            onValueChange = { input: String ->
                                if (input.all { it.isLetter() || it.isWhitespace() }) {
                                    vnpCardName = input
                                    vnpCardError = null
                                }
                            },
                            label = { Text("Tên chủ thẻ (không dấu)") },
                            placeholder = { Text("Ví dụ: NGUYEN VAN A") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = vnpCardExpiry,
                            onValueChange = { input: String ->
                                if (input.length <= 5) {
                                    vnpCardExpiry = input
                                    vnpCardError = null
                                }
                            },
                            label = { Text("Ngày phát hành (MM/YY)") },
                            placeholder = { Text("Ví dụ: 07/15") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        vnpCardError?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                vnpCardNumber = "9704198526191432198"
                                vnpCardName = "NGUYEN VAN A"
                                vnpCardExpiry = "07/15"
                                vnpCardError = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tự động điền thẻ Test NCB", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Mã OTP đã được ngân hàng NCB gửi tới số điện thoại của bạn. Vui lòng xác minh để hoàn tất liên kết thẻ.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { input: String ->
                                if (input.all { it.isDigit() } && input.length <= 6) {
                                    otpCode = input
                                    otpError = null
                                }
                            },
                            label = { Text("Mã xác thực OTP") },
                            placeholder = { Text("Nhập 6 chữ số") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        )

                        otpError?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                otpCode = "123456"
                                otpError = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tự động điền OTP Sandbox (123456)", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (method.id == PaymentMethodCatalog.PAYPAL_ID) {
                    if (showPaypalLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF003087))
                            Text("Đang kết nối bảo mật đến PayPal...", fontWeight = FontWeight.SemiBold, color = Color(0xFF003087))
                        }
                    } else {
                        Text(
                            text = "Đăng nhập tài khoản PayPal Sandbox của bạn để cấp quyền ủy nhiệm thanh toán nhanh.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = paypalEmail,
                            onValueChange = { input: String ->
                                paypalEmail = input
                                paypalError = null
                            },
                            label = { Text("Địa chỉ Email PayPal") },
                            placeholder = { Text("Ví dụ: sandbox-buyer@paypal.com") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = paypalPassword,
                            onValueChange = { input: String ->
                                paypalPassword = input
                                paypalError = null
                            },
                            label = { Text("Mật khẩu") },
                            placeholder = { Text("Nhập mật khẩu") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        paypalError?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                paypalEmail = "sandbox-buyer@paypal.com"
                                paypalPassword = "mypassword123"
                                paypalError = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tự động điền tài khoản PayPal Test", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!showPaypalLoading) {
                Button(
                    enabled = !isSaving && when (method.id) {
                        PaymentMethodCatalog.MOMO_ID, PaymentMethodCatalog.ZALOPAY_ID -> {
                            if (step == 1) phoneNumber.length >= 9 else otpCode.length >= 4
                        }
                        PaymentMethodCatalog.VNPAY_ID -> {
                            if (step == 1) {
                                vnpCardNumber.isNotEmpty() && vnpCardName.isNotEmpty() && vnpCardExpiry.isNotEmpty()
                            } else otpCode.length >= 4
                        }
                        PaymentMethodCatalog.PAYPAL_ID -> paypalEmail.isNotEmpty() && paypalPassword.isNotEmpty()
                        else -> false
                    },
                    onClick = {
                        when (method.id) {
                            PaymentMethodCatalog.MOMO_ID, PaymentMethodCatalog.ZALOPAY_ID -> {
                                if (step == 1) {
                                    step = 2
                                } else {
                                    if (otpCode == "123456") {
                                        onSave("${method.title} - ${phoneNumber.trim()}")
                                    } else {
                                        otpError = "Mã OTP chưa chính xác. Vui lòng dùng mã 123456."
                                    }
                                }
                            }
                            PaymentMethodCatalog.VNPAY_ID -> {
                                if (step == 1) {
                                    if (vnpCardNumber == "9704198526191432198" && vnpCardName.trim().uppercase() == "NGUYEN VAN A" && vnpCardExpiry.trim() == "07/15") {
                                        step = 2
                                    } else {
                                        vnpCardError = "Thông tin thẻ ATM NCB Sandbox chưa chính xác. Vui lòng kiểm tra lại."
                                    }
                                } else {
                                    if (otpCode == "123456") {
                                        onSave("NCB - **** ${vnpCardNumber.takeLast(4)}")
                                    } else {
                                        otpError = "Mã OTP chưa chính xác. Vui lòng dùng mã 123456."
                                    }
                                }
                            }
                            PaymentMethodCatalog.PAYPAL_ID -> {
                                if (paypalEmail.contains("@")) {
                                    showPaypalLoading = true
                                } else {
                                    paypalError = "Địa chỉ email PayPal chưa đúng định dạng."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (method.id) {
                            PaymentMethodCatalog.MOMO_ID, PaymentMethodCatalog.ZALOPAY_ID, PaymentMethodCatalog.VNPAY_ID -> {
                                if (step == 1) "Tiếp tục" else "Xác nhận liên kết"
                            }
                            PaymentMethodCatalog.PAYPAL_ID -> "Đăng nhập & Chấp nhận"
                            else -> "Xác nhận"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (!showPaypalLoading) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Hủy", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
