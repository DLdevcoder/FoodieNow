package com.example.foodienow.feature.customer_home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import java.util.Calendar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.foodienow.R
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.components.formatPrice

@Composable
fun CustomerHomeScreen(
    viewModel: CustomerHomeViewModel = hiltViewModel(),
    onNavigateToFoodDetail: (Food) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        item {
            HomeTopSection(
                onNavigateToSearch = onNavigateToSearch,
                address = uiState.address
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            CategoriesSection()
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalFoodSection(
                title = stringResource(R.string.home_section_good_meal),
                foods = uiState.recommendedFoods,
                isLoading = uiState.isLoading,
                onFoodClick = onNavigateToFoodDetail,
                onSeeAllClick = onNavigateToSearch
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalFoodSection(
                title = stringResource(R.string.home_section_must_try),
                foods = uiState.recommendedFoods.shuffled(),
                isLoading = uiState.isLoading,
                onFoodClick = onNavigateToFoodDetail,
                onSeeAllClick = onNavigateToSearch
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            CollectionsSection(
                foods = uiState.recommendedFoods, 
                isLoading = uiState.isLoading,
                onSeeAllClick = onNavigateToSearch
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp)) // Extra space at bottom
        }
    }
}

@Composable
private fun HomeTopSection(
    onNavigateToSearch: () -> Unit,
    address: String
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> stringResource(R.string.good_morning)
        in 12..17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE65100), // Dark Orange for better status bar contrast
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    greeting,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Deliver To: ",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        address,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable { onNavigateToSearch() }, // Khi báº¥m sáº½ nháº£y sang trang tÃ¬m kiáº¿m
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_search_placeholder), // "Báº¡n muá»‘n Äƒn gÃ¬?"
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CategoriesSection() {
    val categories = listOf(
        R.string.home_category_1, R.string.home_category_2,
        R.string.home_category_3, R.string.home_category_4,
        R.string.home_category_5, R.string.home_category_6,
        R.string.home_category_7, R.string.home_category_8
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val rows = categories.chunked(4)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { catRes ->
                    CategoryItem(catRes)
                }
                // Fill empty spaces if needed
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.width(58.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(labelRes: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(58.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for category icon
            Text("🍲", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            stringResource(labelRes),
            fontSize = 10.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}

@Composable
private fun HorizontalFoodSection(
    title: String,
    foods: List<Food>,
    isLoading: Boolean,
    onFoodClick: (Food) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                stringResource(R.string.home_see_all),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSeeAllClick() }
                    .padding(4.dp)
            )
        }
        
        if (isLoading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(3) {
                    Box(modifier = Modifier.width(112.dp)) {
                        com.example.foodienow.core.designsystem.components.FoodItemShimmer()
                    }
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foods) { food ->
                    FoodCard(food, onFoodClick)
                }
            }
        }
    }
}

@Composable
fun FoodCard(food: Food, onClick: (Food) -> Unit) {
    Card(
        modifier = Modifier
            .width(112.dp)
            .clickable { onClick(food) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .background(Color.LightGray)
            ) {
                if (!food.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)) {
                Text(
                    food.name,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    food.price.formatPrice(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CollectionsSection(foods: List<Food>, isLoading: Boolean, onSeeAllClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.home_section_collections),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                stringResource(R.string.home_see_all),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSeeAllClick() }
                    .padding(4.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(3) {
                    Box(modifier = Modifier.width(104.dp)) {
                        com.example.foodienow.core.designsystem.components.FoodItemShimmer()
                    }
                }
            }
        } else {
            val collectionFoods = foods.take(3)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(collectionFoods) { food ->
                    Card(
                        modifier = Modifier
                            .width(104.dp)
                            .height(136.dp)
                            .clickable { /* Could navigate to a collection page */ },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(78.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                if (!food.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = food.imageUrl,
                                        contentDescription = food.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Text(
                                food.name,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
