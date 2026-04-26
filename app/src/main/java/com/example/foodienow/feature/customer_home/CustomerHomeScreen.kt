package com.example.foodienow.feature.customer_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.components.FoodItemCard

import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.core.designsystem.theme.ColorPrimary
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark
import com.example.foodienow.core.designsystem.theme.ColorSurfaceLight
import com.example.foodienow.feature.customer_home.components.FoodDetailBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: CustomerHomeViewModel = hiltViewModel(),
    onNavigateToCart: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToFoodDetail: (Food) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFoodForSheet by remember { mutableStateOf<Food?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hôm nay ăn gì?",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Thong bao",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Ho so",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary
                )
            )
        },
        containerColor = ColorBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Thanh tìm kiếm
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                placeholder = { Text("Tìm kiếm món ăn, quán ăn...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ColorPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorPrimaryDark,
                    unfocusedBorderColor = ColorSurfaceLight
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToCart,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gio hang")
                }
                Button(
                    onClick = onNavigateToPayment,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Wallet, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thanh toan")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gợi ý",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ColorPrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Danh sách món ăn
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth(), color = ColorPrimaryDark)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.recommendedFoods) { food ->
                        FoodItemCard(
                            food = food,
                            onCardClick = { clickedFood ->
                                // Chuyển sang trang chi tiết
                                onNavigateToFoodDetail(clickedFood)
                            },
                            onAddToCartClick = { clickedFood ->
                                selectedFoodForSheet = clickedFood
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
    selectedFoodForSheet?.let { food ->
        FoodDetailBottomSheet(
            food = food,
            onDismiss = { selectedFoodForSheet = null },
            onAddToCart = { addedFood, quantity ->
                println("Đã thêm ${quantity} món ${addedFood.name} vào giỏ")
                // TODO: Xử lý lưu giỏ hàng
            }
        )
    }
}