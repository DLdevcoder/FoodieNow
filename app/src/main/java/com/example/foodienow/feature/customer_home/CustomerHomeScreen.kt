package com.example.foodienow.feature.customer_home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.CategoryChip
import com.example.foodienow.core.designsystem.components.FoodImage
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.components.FoodieEmptyState
import com.example.foodienow.core.designsystem.components.FoodieErrorState
import com.example.foodienow.core.designsystem.components.FoodieSearchPill
import com.example.foodienow.core.designsystem.components.RatingBadge
import com.example.foodienow.core.designsystem.components.VoucherBadge
import com.example.foodienow.core.designsystem.components.shimmerEffect
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieDiscount
import com.example.foodienow.core.designsystem.theme.FoodieRating
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.domain.model.Category
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import com.example.foodienow.feature.customer_home.components.formatPrice
import java.util.Calendar

@Composable
fun CustomerHomeScreen(
    viewModel: CustomerHomeViewModel = hiltViewModel(),
    onNavigateToFoodDetail: (Food) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCategory: (categoryId: String, categoryName: String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToChatList: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Bổ sung LaunchedEffect để update lại số đếm khi quay lại trang chủ
    LaunchedEffect(Unit) {
        viewModel.loadUnreadMessageCount()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(FoodieCream),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeTopSection(
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToChatList = onNavigateToChatList,
                    address = uiState.address,
                    unreadMessageCount = uiState.unreadMessageCount
                )
            }

            if (uiState.errorMessage != null && uiState.recommendedFoods.isEmpty()) {
                item {
                    FoodieErrorState(
                        title = "Chưa tải được thực đơn",
                        subtitle = uiState.errorMessage.orEmpty(),
                        actionLabel = "Thử lại",
                        onAction = viewModel::refresh
                    )
                }
            } else {
                item {
                    CategoriesSection(
                        categories = uiState.categories,
                        isLoading = uiState.isLoading,
                        onCategoryClick = { category ->
                            val categoryId = category.id
                            if (categoryId != null) {
                                onNavigateToCategory(categoryId, category.name)
                            } else {
                                onNavigateToSearch()
                            }
                        }
                    )
                }

                item {
                    HorizontalFoodSection(
                        title = stringResource(R.string.home_section_good_meal),
                        subtitle = "Món bán chạy quanh bạn, giao nhanh trong hôm nay",
                        foods = uiState.recommendedFoods.sortedByDescending { it.soldCount },
                        isLoading = uiState.isLoading,
                        onFoodClick = onNavigateToFoodDetail,
                        onSeeAllClick = onNavigateToSearch
                    )
                }

                if (uiState.isLoading || uiState.featuredStores.isNotEmpty()) {
                    item {
                        RestaurantsSection(
                            stores = uiState.featuredStores,
                            isLoading = uiState.isLoading,
                            onSeeAllClick = onNavigateToSearch
                        )
                    }
                }

                item {
                    HorizontalFoodSection(
                        title = stringResource(R.string.home_section_must_try),
                        subtitle = "Gợi ý được chọn theo đánh giá và lượt đặt",
                        foods = uiState.recommendedFoods.sortedWith(
                            compareByDescending<Food> { it.rating }.thenByDescending { it.soldCount }
                        ),
                        isLoading = uiState.isLoading,
                        onFoodClick = onNavigateToFoodDetail,
                        onSeeAllClick = onNavigateToSearch
                    )
                }

                item {
                    CollectionsSection(onSeeAllClick = onNavigateToSearch)
                }
            }
        }
    }
}

@Composable
private fun HomeTopSection(
    onNavigateToSearch: () -> Unit,
    onNavigateToChatList: () -> Unit,
    address: String,
    unreadMessageCount: Int
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
                Brush.verticalGradient(
                    listOf(
                        PromoGradientStart,
                        MaterialTheme.colorScheme.primary,
                        PromoGradientEnd
                    )
                )
            )
            .statusBarsPadding()
            .padding(start = 18.dp, top = 2.dp, end = 18.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.78f)) {
                Text(
                    text = "FoodieNow",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            DeliverySummary(
                address = address,
                modifier = Modifier
                    .weight(1.42f)
                    .widthIn(min = 156.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodieSearchPill(
                text = stringResource(R.string.home_search_placeholder),
                onClick = onNavigateToSearch,
                modifier = Modifier.weight(1f)
            )
            HeaderActionButton(
                icon = Icons.Default.ChatBubbleOutline,
                contentDescription = "Tin nhắn",
                badgeCount = unreadMessageCount,
                onClick = onNavigateToChatList
            )
        }
    }
}

@Composable
private fun DeliverySummary(
    address: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Giao đến",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.74f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = address,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SearchCtaButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .width(54.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
        .background(Brush.linearGradient(listOf(Color.White, Color(0xFFFFF1E8)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = "Tìm kiếm",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(23.dp)
        )
    }
}

@Composable
private fun HeaderActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.White.copy(alpha = 0.18f),
            contentColor = Color.White
        )
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(containerColor = FoodieDiscount) {
                        Text(text = badgeCount.coerceAtMost(99).toString(), color = Color.White)
                    }
                }
            ) {
                Icon(icon, contentDescription = contentDescription)
            }
        } else {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}

@Composable
private fun PromoBanner(
    imageUrl: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .padding(horizontal = 18.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .background(Brush.linearGradient(listOf(PromoGradientStart, PromoGradientEnd)))
    ) {
        AsyncImage(
            model = imageUrl ?: "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?q=80&w=1200",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.34f)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xE620130D),
                            Color(0x996B2108),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VoucherBadge(label = "FREESHIP", containerColor = Color.White.copy(alpha = 0.22f))
                Text(
                    text = "Đặt món nóng hổi, giao ngay",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ưu đãi đến 50% cho bữa trưa và tối nay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = CircleShape,
                color = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đặt ngay",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<Category>,
    isLoading: Boolean,
    onCategoryClick: (Category) -> Unit
) {
    val displayCategories = if (categories.isNotEmpty()) {
        categories.take(10)
    } else {
        listOf(
            Category(name = stringResource(R.string.home_category_1)),
            Category(name = stringResource(R.string.home_category_2)),
            Category(name = stringResource(R.string.home_category_3)),
            Category(name = stringResource(R.string.home_category_4)),
            Category(name = stringResource(R.string.home_category_5)),
            Category(name = stringResource(R.string.home_category_8))
        )
    }

    SectionContainer {
        SectionHeader(
            title = "Bạn đang thèm gì?",
            subtitle = "Chọn nhanh theo món quen của người Việt",
            onSeeAllClick = null
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading && categories.isEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(4) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(MaterialTheme.shapes.large)
                                    .shimmerEffect()
                            )
                        }
                    }
                }
            }
        } else {
            val categoryColumns = displayCategories.chunked(2)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categoryColumns) { columnCategories ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        columnCategories.forEach { category ->
                            CategoryChip(
                                label = category.name,
                                imageUrl = category.imageUrl,
                                onClick = { onCategoryClick(category) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalFoodSection(
    title: String,
    subtitle: String,
    foods: List<Food>,
    isLoading: Boolean,
    onFoodClick: (Food) -> Unit,
    onSeeAllClick: () -> Unit
) {
    SectionContainer {
        SectionHeader(title = title, subtitle = subtitle, onSeeAllClick = onSeeAllClick)

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(3) { FoodCardSkeleton() }
                }
            }

            foods.isEmpty() -> {
                FoodieEmptyState(
                    title = "Chưa có món phù hợp",
                    subtitle = "Các món mới sẽ xuất hiện tại đây khi quán cập nhật thực đơn.",
                    actionLabel = "Tìm món khác",
                    onAction = onSeeAllClick,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            else -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(foods.take(10)) { food ->
                        FoodCard(
                            food = food,
                            onClick = onFoodClick,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RestaurantsSection(
    stores: List<Store>,
    isLoading: Boolean,
    onSeeAllClick: () -> Unit
) {
    SectionContainer {
        SectionHeader(
            title = "Quán nổi bật gần bạn",
            subtitle = "Đánh giá tốt, chuẩn bị nhanh",
            onSeeAllClick = onSeeAllClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(3) { RestaurantCardSkeleton() }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(stores.take(6)) { store ->
                    CompactRestaurantCard(
                        name = store.name,
                        imageUrl = store.imageUrl,
                        address = store.address,
                        rating = store.rating,
                        reviewCount = store.reviewCount,
                        onClick = onSeeAllClick,
                        modifier = Modifier.width(198.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactRestaurantCard(
    name: String,
    imageUrl: String?,
    address: String?,
    rating: Double?,
    reviewCount: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FoodieCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box {
                FoodImage(
                    imageUrl = imageUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                )

                if (rating != null && rating > 0) {
                    RatingBadge(
                        rating = rating,
                        reviewCount = reviewCount,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!address.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "20-30 phút",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CollectionsSection(onSeeAllClick: () -> Unit) {
    SectionContainer {
        SectionHeader(
            title = stringResource(R.string.home_section_collections),
            subtitle = "Voucher và combo đang được dùng nhiều",
            onSeeAllClick = onSeeAllClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                VoucherDealCard(
                    title = "FREESHIP đơn từ 50K",
                    subtitle = "Quán gần bạn",
                    badge = "Giao nhanh",
                    containerBrush = Brush.linearGradient(listOf(Color(0xFF0E8A73), Color(0xFF21B493))),
                    onClick = onSeeAllClick
                )
            }
            item {
                VoucherDealCard(
                    title = "Combo trưa tiết kiệm",
                    subtitle = "Giảm đến 50%",
                    badge = "Hot deal",
                    containerBrush = Brush.linearGradient(listOf(PromoGradientStart, PromoGradientEnd)),
                    onClick = onSeeAllClick
                )
            }
            item {
                VoucherDealCard(
                    title = "Món ngon dưới 39K",
                    subtitle = "Ăn nhanh, giá mềm",
                    badge = "Tiết kiệm",
                    containerBrush = Brush.linearGradient(listOf(Color(0xFFEF4444), PromoGradientStart)),
                    onClick = onSeeAllClick
                )
            }
        }
    }
}

@Composable
private fun VoucherDealCard(
    title: String,
    subtitle: String,
    badge: String,
    containerBrush: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(208.dp)
            .height(96.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .background(containerBrush)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Discount, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                VoucherBadge(label = badge, containerColor = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FoodCard(
    food: Food,
    onClick: (Food) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    FoodieCard(
        modifier = modifier,
        onClick = { onClick(food) }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box {
                FoodImage(
                    imageUrl = food.imageUrl,
                    contentDescription = food.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.28f)
                )
                if (food.soldCount >= 1000) {
                    VoucherBadge(
                        label = "Bán chạy",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = food.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingSummary(rating = food.rating, soldCount = food.soldCount)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.price.formatPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PromoGradientStart, PromoGradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingSummary(rating: Double, soldCount: Int) {
    val soldText = if (soldCount > 0) "Đã bán ${compactCount(soldCount)}" else "Món mới"

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = FoodieRating.copy(alpha = 0.14f),
            contentColor = FoodieRating
        ) {
            Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(13.dp))
                Text(
                    text = if (rating > 0) String.format("%.1f", rating) else "Mới",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = soldText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAllClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onSeeAllClick != null) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onSeeAllClick)
                    .padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xem thêm",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FoodCardSkeleton() {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.28f)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .height(18.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.54f)
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(20.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        )
    }
}

@Composable
private fun RestaurantCardSkeleton() {
    Column(
        modifier = Modifier
            .width(198.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .height(12.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        )
    }
}

private fun compactCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}tr+"
        count >= 1_000 -> {
            val whole = count / 1_000
            val decimal = (count % 1_000) / 100
            if (decimal == 0) "${whole}k+" else "$whole,${decimal}k+"
        }
        count > 0 -> count.toString()
        else -> "mới"
    }
}
