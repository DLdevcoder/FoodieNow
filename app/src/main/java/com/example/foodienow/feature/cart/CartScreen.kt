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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.core.designsystem.theme.ColorPrimary
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark
import com.example.foodienow.core.designsystem.theme.ColorSurfaceLight
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
                title = { Text("Giỏ hàng của bạn", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorPrimary)
            )
        },
        containerColor = ColorBackground,
        bottomBar = {
            // Thanh này luôn hiển thị, tự tính toán dựa trên giỏ hàng
            val totalPrice = if (cartItems.isNotEmpty()) cartItems.entries.sumOf { it.key.price * it.value } else 0.0

            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
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
                        Text(text = "Tổng thanh toán", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = totalPrice.formatPrice(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimaryDark
                        )
                    }

                    Button(
                        onClick = { viewModel.onCheckoutClicked() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimaryDark,
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp),
                        enabled = cartItems.isNotEmpty() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Thanh toán", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Giỏ hàng của bạn đang trống", color = Color.Gray, fontWeight = FontWeight.Bold)
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

// Giữ nguyên CartItemCard bên dưới ...
@Composable
fun CartItemCard(
    food: Food,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = food.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = food.name, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = food.price.formatPrice(), color = ColorPrimaryDark, fontWeight = FontWeight.Bold)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(ColorSurfaceLight, RoundedCornerShape(50)).padding(2.dp)
            ) {
                IconButton(onClick = { onQuantityChange(quantity - 1) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = ColorPrimaryDark, modifier = Modifier.size(16.dp))
                }
                Text(text = quantity.toString(), fontWeight = FontWeight.Bold, color = ColorPrimaryDark, modifier = Modifier.padding(horizontal = 4.dp))
                IconButton(onClick = { onQuantityChange(quantity + 1) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Tăng", tint = ColorPrimaryDark, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}