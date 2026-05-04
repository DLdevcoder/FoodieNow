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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.components.FoodItemCard

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
                            placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    isSearchExpanded = false
                                    viewModel.onSearchQueryChange("") // Xóa query khi đóng
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.home_search_cancel),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Text(
                            stringResource(R.string.app_name),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.home_search_action),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home_nav_home)) },
                    label = { Text(stringResource(R.string.home_nav_home), fontWeight = FontWeight.SemiBold) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(R.string.home_nav_cart)) },
                    label = { Text(stringResource(R.string.home_nav_cart), fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = onNavigateToCart,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.home_nav_notifications)) },
                    label = { Text(stringResource(R.string.home_nav_notifications), fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = onNavigateToNotifications,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.home_nav_profile)) },
                    label = { Text(stringResource(R.string.home_nav_profile), fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = onNavigateToProfile,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {

            if (isSearchExpanded && uiState.searchQuery.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize())
            } else if (uiState.searchQuery.isNotEmpty()) {
                Text(
                    stringResource(R.string.home_search_results),
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = FontWeight.Bold
                )

                val displayList = uiState.searchResults

                if (displayList.isEmpty()) {
                    Text(stringResource(R.string.home_search_empty), color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
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
                    stringResource(R.string.home_recommendations),
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
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