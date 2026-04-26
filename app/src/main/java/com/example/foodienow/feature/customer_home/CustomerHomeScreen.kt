package com.example.foodienow.feature.customer_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.foodienow.feature.cart.CartViewModel
import com.example.foodienow.feature.customer_home.components.FoodDetailBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: CustomerHomeViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToFoodDetail: (Food) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedFoodForSheet by remember { mutableStateOf<Food?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            placeholder = { Text("Bạn muốn ăn gì?") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    isSearchExpanded = false
                                    viewModel.onSearchQueryChange("") // Xóa query khi đóng
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hủy", tint = Color.Gray)
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("FoodieNow", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorPrimary)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ", fontWeight = FontWeight.SemiBold) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryDark,
                        selectedTextColor = ColorPrimaryDark,
                        indicatorColor = ColorSurfaceLight
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng") },
                    label = { Text("Giỏ hàng", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = onNavigateToCart,
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Thông báo") },
                    label = { Text("Thông báo", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = onNavigateToNotifications,
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Hồ sơ") },
                    label = { Text("Hồ sơ", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = onNavigateToProfile,
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                )
            }
        },
        containerColor = ColorBackground
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {

            if (isSearchExpanded && uiState.searchQuery.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize())
            } else if (uiState.searchQuery.isNotEmpty()) {
                Text(
                    "Kết quả tìm kiếm",
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = FontWeight.Bold
                )

                // THAY ĐỔI Ở ĐÂY: Sử dụng searchResults
                val displayList = uiState.searchResults

                if (displayList.isEmpty()) {
                    Text("Không tìm thấy món ăn nào phù hợp.", color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f), // Nhớ giữ weight(1f) để tránh lỗi tàng hình
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayList) { food ->
                            FoodItemCard(
                                food = food,
                                onCardClick = { onNavigateToFoodDetail(it) },
                                onAddToCartClick = { selectedFoodForSheet = it }
                            )
                        }
                    }
                }
            } else {
                Text(
                    "Gợi ý",
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimaryDark
                )
                LazyColumn(
                    modifier = Modifier.weight(1f), // Nhớ giữ weight(1f)
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recommendedFoods) { food ->
                        FoodItemCard(
                            food = food,
                            onCardClick = { onNavigateToFoodDetail(it) },
                            onAddToCartClick = { selectedFoodForSheet = it }
                        )
                    }
                }
            }
        }
    }

    selectedFoodForSheet?.let { food ->
        FoodDetailBottomSheet(
            food = food,
            onDismiss = { selectedFoodForSheet = null },
            onAddToCart = { addedFood, quantity ->
                cartViewModel.addToCart(addedFood, quantity)
                selectedFoodForSheet = null
            }
        )
    }
}