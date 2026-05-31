package com.example.foodienow.feature.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Voucher
import com.example.foodienow.feature.customer_home.components.formatPrice

@Composable
fun MerchantMenuTab(
    uiState: MerchantUiState,
    onToggleAvailability: (Food) -> Unit,
    onAddFoodClick: () -> Unit,
    onEditFoodClick: (Food) -> Unit,
    onCreateVoucher: (String, Int, Long, Long, Long, Boolean, String?) -> Unit,
    onUpdateVoucher: (String, String, Int, Long, Long, Long, Boolean, String?) -> Unit,
    onDeleteVoucher: (String) -> Unit
) {
    var subTabSelected by remember { mutableIntStateOf(0) }
    var showVoucherDialog by remember { mutableStateOf(false) }
    var editingVoucher by remember { mutableStateOf<Voucher?>(null) }
    var voucherToDelete by remember { mutableStateOf<Voucher?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = Color.Transparent,
            contentColor = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
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
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(27.dp),
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.merchant_menu_manage_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.merchant_menu_manage_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.84f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        TabRow(
            selectedTabIndex = subTabSelected,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = subTabSelected == 0,
                onClick = { subTabSelected = 0 },
                text = { Text(stringResource(R.string.merchant_tab_menu), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTabSelected == 1,
                onClick = { subTabSelected = 1 },
                text = { Text(stringResource(R.string.merchant_tab_voucher), fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (subTabSelected == 0) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Text(
                        text = stringResource(R.string.error_prefix, uiState.error),
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.menu.isEmpty()) {
                    Text(
                        text = stringResource(R.string.merchant_menu_empty_food),
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.menu) { food ->
                            MerchantFoodItem(
                                food = food,
                                onToggle = { onToggleAvailability(food) },
                                onEdit = { onEditFoodClick(food) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                FloatingActionButton(
                    onClick = onAddFoodClick,
                    containerColor = ColorPrimaryDark,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.merchant_menu_add_food_desc))
                }
            } else {
                if (uiState.isVouchersLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.vouchers.isEmpty()) {
                    Text(
                        text = stringResource(R.string.merchant_menu_empty_voucher),
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.vouchers) { voucher ->
                            MerchantVoucherItem(
                                voucher = voucher,
                                onEdit = {
                                    editingVoucher = voucher
                                    showVoucherDialog = true
                                },
                                onDelete = {
                                    voucherToDelete = voucher
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        editingVoucher = null
                        showVoucherDialog = true
                    },
                    containerColor = ColorPrimaryDark,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.merchant_menu_add_voucher_desc))
                }
            }
        }
    }

    if (showVoucherDialog) {
        AddEditVoucherDialog(
            voucher = editingVoucher,
            onDismiss = { showVoucherDialog = false },
            onConfirm = { code, percent, amount, minVal, maxDis, active, expiry ->
                if (editingVoucher == null) {
                    onCreateVoucher(code, percent, amount, minVal, maxDis, active, expiry)
                } else {
                    onUpdateVoucher(editingVoucher!!.id, code, percent, amount, minVal, maxDis, active, expiry)
                }
                showVoucherDialog = false
            }
        )
    }

    if (voucherToDelete != null) {
        AlertDialog(
            onDismissRequest = { voucherToDelete = null },
            title = { Text(stringResource(R.string.merchant_voucher_delete_confirm_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.merchant_voucher_delete_confirm_desc, voucherToDelete?.code ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        voucherToDelete?.let { onDeleteVoucher(it.id) }
                        voucherToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.common_delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { voucherToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun MerchantVoucherItem(
    voucher: Voucher,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    FoodieCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight()
                    .background(ColorPrimaryDark.copy(alpha = 0.08f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val valueText = if (voucher.discountPercent > 0) {
                        stringResource(R.string.merchant_voucher_percent_suffix, voucher.discountPercent.toString())
                    } else {
                        stringResource(R.string.merchant_voucher_k_suffix, (voucher.discountAmount / 1000).toString())
                    }
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorPrimaryDark,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.merchant_voucher_discount_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimaryDark.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = voucher.code,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (voucher.isActive) Color(0xFF4CAF50) else Color.Red)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.merchant_voucher_min_order_prefix, voucher.minOrderValue.formatPrice()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    if (voucher.discountPercent > 0 && voucher.maxDiscount > 0) {
                        Text(
                            text = stringResource(R.string.merchant_voucher_max_discount_prefix, voucher.maxDiscount.formatPrice()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val expiryText = voucher.expiresAt?.let {
                        stringResource(R.string.merchant_voucher_expiry_prefix, it.take(10).split("-").reversed().joinToString("/"))
                    } ?: stringResource(R.string.merchant_voucher_no_expiry)
                    Text(
                        text = expiryText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.common_edit),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.common_delete),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantFoodItem(
    food: Food,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    FoodieCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = food.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = food.price.formatPrice(),
                    color = ColorPrimaryDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (food.isAvailable) Color(0xFF4CAF50) else Color.Red)
                    )
                    Text(
                        text = if (food.isAvailable) stringResource(R.string.merchant_food_status_available) else stringResource(R.string.merchant_food_status_unavailable),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (food.isAvailable) Color(0xFF4CAF50) else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.common_edit),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Switch(
                    checked = food.isAvailable,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.7f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorPrimaryDark,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.LightGray.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVoucherDialog(
    voucher: Voucher?,
    onDismiss: () -> Unit,
    onConfirm: (
        code: String,
        discountPercent: Int,
        discountAmount: Long,
        minOrderValue: Long,
        maxDiscount: Long,
        isActive: Boolean,
        expiresAt: String?
    ) -> Unit
) {
    var code by remember(voucher) { mutableStateOf(voucher?.code ?: "") }
    var isPercent by remember(voucher) { mutableStateOf(voucher == null || voucher.discountPercent > 0) }
    var discountPercent by remember(voucher) { mutableStateOf(voucher?.discountPercent?.toString() ?: "") }
    var discountAmount by remember(voucher) { mutableStateOf(voucher?.discountAmount?.toString() ?: "") }
    var minOrderValue by remember(voucher) { mutableStateOf(voucher?.minOrderValue?.toString() ?: "") }
    var maxDiscount by remember(voucher) { mutableStateOf(voucher?.maxDiscount?.toString() ?: "") }
    var isActive by remember(voucher) { mutableStateOf(voucher?.isActive ?: true) }
    var expiresAt by remember(voucher) { mutableStateOf(voucher?.expiresAt?.take(10) ?: "") }

    var codeError by remember { mutableStateOf<String?>(null) }
    var discountError by remember { mutableStateOf<String?>(null) }
    var minOrderError by remember { mutableStateOf<String?>(null) }
    var maxDiscountError by remember { mutableStateOf<String?>(null) }
    var expiresError by remember { mutableStateOf<String?>(null) }

    val errorEmptyCode = stringResource(R.string.merchant_voucher_error_empty_code)
    val errorEmpty = stringResource(R.string.merchant_voucher_error_empty)
    val errorPercentRange = stringResource(R.string.merchant_voucher_error_percent_range)
    val errorAmountPositive = stringResource(R.string.merchant_voucher_error_amount_positive)
    val errorMaxAmount = stringResource(R.string.merchant_voucher_error_max_amount)
    val errorMinOrder = stringResource(R.string.merchant_voucher_error_min_order)
    val errorExpiryFormat = stringResource(R.string.merchant_voucher_error_expiry_format)
    val errorDiscountInvalid = stringResource(R.string.merchant_voucher_error_discount_invalid)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (voucher == null) stringResource(R.string.merchant_voucher_add_title) else stringResource(R.string.merchant_voucher_edit_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        codeError = if (it.isBlank()) errorEmptyCode else null
                    },
                    label = { Text(stringResource(R.string.merchant_voucher_code_label)) },
                    placeholder = { Text(stringResource(R.string.merchant_voucher_code_hint)) },
                    isError = codeError != null,
                    supportingText = codeError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isPercent,
                        onClick = { isPercent = true },
                        label = { Text(stringResource(R.string.merchant_voucher_type_percent)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isPercent,
                        onClick = { isPercent = false },
                        label = { Text(stringResource(R.string.merchant_voucher_type_fixed)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isPercent) {
                    OutlinedTextField(
                        value = discountPercent,
                        onValueChange = {
                            discountPercent = it
                            discountError = when {
                                it.isBlank() -> errorEmpty
                                it.toIntOrNull() == null || it.toInt() !in 1..100 -> errorPercentRange
                                else -> null
                            }
                        },
                        label = { Text(stringResource(R.string.merchant_voucher_percent_label)) },
                        placeholder = { Text(stringResource(R.string.merchant_voucher_percent_hint)) },
                        isError = discountError != null,
                        supportingText = discountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = {
                            maxDiscount = it
                            maxDiscountError = when {
                                it.isNotBlank() && it.toLongOrNull() == null -> errorMaxAmount
                                else -> null
                            }
                        },
                        label = { Text(stringResource(R.string.merchant_voucher_max_amount_label)) },
                        placeholder = { Text(stringResource(R.string.merchant_voucher_max_amount_hint)) },
                        isError = maxDiscountError != null,
                        supportingText = maxDiscountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = discountAmount,
                        onValueChange = {
                            discountAmount = it
                            discountError = when {
                                it.isBlank() -> errorEmpty
                                it.toLongOrNull() == null || it.toLong() <= 0 -> errorAmountPositive
                                else -> null
                            }
                        },
                        label = { Text(stringResource(R.string.merchant_voucher_amount_label)) },
                        placeholder = { Text(stringResource(R.string.merchant_voucher_amount_hint)) },
                        isError = discountError != null,
                        supportingText = discountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = minOrderValue,
                    onValueChange = {
                        minOrderValue = it
                        minOrderError = when {
                            it.isBlank() -> errorEmpty
                            it.toLongOrNull() == null || it.toLong() < 0 -> errorMinOrder
                            else -> null
                        }
                    },
                    label = { Text(stringResource(R.string.merchant_voucher_min_order_label)) },
                    placeholder = { Text(stringResource(R.string.merchant_voucher_min_order_hint)) },
                    isError = minOrderError != null,
                    supportingText = minOrderError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = expiresAt,
                    onValueChange = {
                        expiresAt = it
                        expiresError = when {
                            it.isNotBlank() && !it.matches(Regex("""^\d{4}-\d{2}-\d{2}$""")) -> errorExpiryFormat
                            else -> null
                        }
                    },
                    label = { Text(stringResource(R.string.merchant_voucher_expiry_label)) },
                    placeholder = { Text(stringResource(R.string.merchant_voucher_expiry_hint)) },
                    isError = expiresError != null,
                    supportingText = expiresError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.merchant_voucher_status_label), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val codeValid = code.isNotBlank()
                    if (!codeValid) {
                        codeError = errorEmptyCode
                    }

                    val discountValid = if (isPercent) {
                        discountPercent.toIntOrNull() != null && discountPercent.toInt() in 1..100
                    } else {
                        discountAmount.toLongOrNull() != null && discountAmount.toLong() > 0
                    }
                    if (!discountValid) {
                        discountError = errorDiscountInvalid
                    }

                    val minOrderValid = minOrderValue.toLongOrNull() != null && minOrderValue.toLong() >= 0
                    if (!minOrderValid) {
                        minOrderError = errorMinOrder
                    }

                    val maxDiscountValid = !isPercent || maxDiscount.isBlank() || maxDiscount.toLongOrNull() != null
                    if (!maxDiscountValid) {
                        maxDiscountError = errorMaxAmount
                    }

                    val expiresValid = expiresAt.isBlank() || expiresAt.matches(Regex("""^\d{4}-\d{2}-\d{2}$"""))
                    if (!expiresValid) {
                        expiresError = errorExpiryFormat
                    }

                    if (codeValid && discountValid && minOrderValid && maxDiscountValid && expiresValid) {
                        val finalPercent = if (isPercent) discountPercent.toInt() else 0
                        val finalAmount = if (isPercent) 0L else discountAmount.toLong()
                        val finalMinOrder = minOrderValue.toLong()
                        val finalMaxDiscount = if (isPercent) maxDiscount.toLongOrNull() ?: 0L else 0L
                        val finalExpiry = if (expiresAt.isBlank()) null else "${expiresAt}T23:59:59Z"

                        onConfirm(
                            code,
                            finalPercent,
                            finalAmount,
                            finalMinOrder,
                            finalMaxDiscount,
                            isActive,
                            finalExpiry
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

private fun Modifier.scale(scale: Float) = this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))