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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.foodienow.core.designsystem.components.RestaurantCard
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
                    address = uiState.address,
                    unreadMessageCount = uiState.unreadMessageCount,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToCart = onNavigateToCart,
                    onNavigateToChatList = onNavigateToChatList
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
                    PromoBanner(
                        imageUrl = uiState.recommendedFoods.firstOrNull()?.imageUrl,
                        onClick = onNavigateToSearch
                    )
                }

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
    address: String,
    unreadMessageCount: Int,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToChatList: () -> Unit
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
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            HeaderActionButton(
                icon = Icons.Default.ShoppingCart,
                contentDescription = "Giỏ hàng",
                onClick = onNavigateToCart
            )
            HeaderActionButton(
                icon = Icons.Default.ChatBubbleOutline,
                contentDescription = "Tin nhắn",
                badgeCount = unreadMessageCount,
                onClick = onNavigateToChatList
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            shape = MaterialTheme.shapes.large,
            color = Color.White.copy(alpha = 0.16f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Giao đến",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.74f)
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        FoodieSearchPill(
            text = stringResource(R.string.home_search_placeholder),
            onClick = onNavigateToSearch,
            modifier = Modifier.fillMaxWidth()
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
                items(5) {
                    Box(
                        modifier = Modifier
                            .width(118.dp)
                            .height(44.dp)
                            .clip(MaterialTheme.shapes.large)
                            .shimmerEffect()
                    )
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(displayCategories) { category ->
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(foods.take(10)) { food ->
                        FoodCard(
                            food = food,
                            onClick = onFoodClick,
                            modifier = Modifier.width(178.dp)
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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(2) { RestaurantCardSkeleton() }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(stores.take(6)) { store ->
                    RestaurantCard(
                        name = store.name,
                        imageUrl = store.imageUrl,
                        address = store.address,
                        rating = store.rating,
                        reviewCount = store.reviewCount,
                        onClick = onSeeAllClick,
                        modifier = Modifier.width(258.dp)
                    )
                }
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

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            VoucherDealCard(
                title = "FREESHIP đơn từ 50K",
                subtitle = "Áp dụng cho quán trong bán kính gần bạn",
                badge = "Giao nhanh",
                containerBrush = Brush.linearGradient(listOf(Color(0xFF0E8A73), Color(0xFF21B493))),
                onClick = onSeeAllClick
            )
            VoucherDealCard(
                title = "Combo bữa trưa tiết kiệm",
                subtitle = "Giảm đến 50% cho món chính kèm nước",
                badge = "Hot deal",
                containerBrush = Brush.linearGradient(listOf(PromoGradientStart, PromoGradientEnd)),
                onClick = onSeeAllClick
            )
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
            .fillMaxWidth()
            .height(112.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .background(containerBrush)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Discount, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                VoucherBadge(label = badge, containerColor = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
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
        Column(modifier = Modifier.padding(10.dp)) {
            Box {
                FoodImage(
                    imageUrl = food.imageUrl,
                    contentDescription = food.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.18f)
                )
                if (food.soldCount >= 1000) {
                    VoucherBadge(
                        label = "Bán chạy",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = food.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingSummary(rating = food.rating, soldCount = food.soldCount)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.price.formatPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
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
            .width(178.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.18f)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(12.dp))
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
            .width(258.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(18.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(14.dp)
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
