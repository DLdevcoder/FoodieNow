package com.example.foodienow.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.foodienow.R
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.components.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCheckout: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItems = uiState.cartItems

    LaunchedEffect(Unit) {
        viewModel.cartEvent.collect { event ->
            when (event) {
                is CartEvent.NavigateToLogin -> onNavigateToLogin()
                is CartEvent.NavigateToCheckout -> onNavigateToCheckout(event.userId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cart_title), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = MaterialTheme.colorScheme.onSurface)
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Thanh này luôn hiển thị, tự tính toán dựa trên giỏ hàng
            val totalPrice = if (cartItems.isNotEmpty()) cartItems.entries.sumOf { it.key.price * it.value } else 0.0

            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = stringResource(R.string.cart_total_label), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = totalPrice.formatPrice(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { viewModel.onCheckoutClicked() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.height(52.dp),
                        enabled = cartItems.isNotEmpty() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text(stringResource(R.string.cart_checkout), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.cart_empty), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems.keys.toList()) { food ->
                    val quantity = cartItems[food] ?: 0
                    CartItemCard(
                        food = food,
                        quantity = quantity,
                        onQuantityChange = { newQty ->
                            viewModel.updateQuantity(food, newQty)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    food: Food,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = food.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = food.name, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = food.price.formatPrice(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.large)
                    .padding(2.dp)
            ) {
                IconButton(onClick = { onQuantityChange(quantity - 1) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.cart_decrease), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Text(text = quantity.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp))
                IconButton(onClick = { onQuantityChange(quantity + 1) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cart_increase), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}