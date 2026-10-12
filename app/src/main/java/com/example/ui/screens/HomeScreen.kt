package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.GameDetail
import com.example.data.model.PlatformType
import com.example.ui.components.CountrySelectorDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val games by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val alerts by viewModel.alertHistory.collectAsState()
    val wishlist by viewModel.wishlistItems.collectAsState()

    var showCountryDialog by remember { mutableStateOf(false) }

    if (showCountryDialog) {
        CountrySelectorDialog(
            selectedCountry = selectedCountry,
            onCountrySelected = { viewModel.setCountry(it) },
            onDismiss = { showCountryDialog = false }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GamerBg)
                    .statusBarsPadding()
            ) {
                // Top App Bar Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Icon + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonCyan,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = "GameRadar",
                                    tint = GamerBg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GameRadar",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = GamerTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Global Game & Deal Tracker",
                                fontSize = 11.sp,
                                color = GamerTextSecondary
                            )
                        }
                    }

                    // Country Selector Pill
                    Surface(
                        onClick = { showCountryDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = GamerSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                        modifier = Modifier.testTag("country_picker_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedCountry.flagEmoji,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedCountry.currencyCode,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change country",
                                tint = GamerTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Alerts Bell
                    IconButton(
                        onClick = onNavigateToAlerts,
                        modifier = Modifier.testTag("alerts_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (alerts.isNotEmpty()) {
                                    Badge(
                                        containerColor = NeonPink,
                                        contentColor = Color.White
                                    ) {
                                        Text("${alerts.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = GamerTextPrimary
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Search any console or PC game...",
                            color = GamerTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonCyan
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = GamerTextSecondary
                                )
                            }
                        } else if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GamerSurface,
                        unfocusedContainerColor = GamerSurface,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GamerBorder,
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("search_games_input")
                )

                // Platform Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PlatformType.values()) { platform ->
                        val isSelected = selectedPlatform == platform
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setPlatform(platform) },
                            label = {
                                Text(
                                    text = platform.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = GamerBg,
                                containerColor = GamerSurfaceElevated,
                                labelColor = GamerTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NeonCyan else GamerBorder,
                                selectedBorderColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("platform_chip_${platform.name}")
                        )
                    }
                }

                // Sort Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Sort:",
                        fontSize = 12.sp,
                        color = GamerTextMuted,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SortOption.values()) { sort ->
                            val isSelected = selectedSort == sort
                            Surface(
                                onClick = { viewModel.setSort(sort) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) GamerSurfaceHighlight else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonPurple) else null,
                                modifier = Modifier.testTag("sort_chip_${sort.name}")
                            ) {
                                Text(
                                    text = sort.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonPurple else GamerTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = GamerBg,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Featured Deal Hero Card (when not searching a specific query)
            if (searchQuery.isEmpty() && games.isNotEmpty()) {
                val heroGame = games.first()
                item {
                    FeaturedHeroDealCard(
                        game = heroGame,
                        currencySymbol = selectedCountry.currencySymbol,
                        rate = selectedCountry.exchangeRateToUsd,
                        onClick = { onNavigateToDetail(heroGame.id) }
                    )
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "🎮 Tracked Games & Live Deals" else "🔍 Results for \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${games.size} games",
                        fontSize = 12.sp,
                        color = GamerTextMuted
                    )
                }
            }

            if (games.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VideogameAssetOff,
                                contentDescription = "No games found",
                                tint = GamerTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No games found",
                                style = MaterialTheme.typography.titleMedium,
                                color = GamerTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search terms or platform filter.",
                                fontSize = 13.sp,
                                color = GamerTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(games, key = { it.id }) { game ->
                    val isWishlisted = wishlist.any { it.gameId == game.id }
                    GameItemCard(
                        game = game,
                        currencySymbol = selectedCountry.currencySymbol,
                        exchangeRate = selectedCountry.exchangeRateToUsd,
                        isWishlisted = isWishlisted,
                        onWishlistToggle = {
                            viewModel.toggleWishlist(game)
                        },
                        onClick = { onNavigateToDetail(game.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedHeroDealCard(
    game: GameDetail,
    currencySymbol: String,
    rate: Double,
    onClick: () -> Unit
) {
    val bestPrice = game.lowestHistoricalPriceUsd * rate
    val retailPrice = game.basePriceUsd * rate
    val savingsPercent = (((retailPrice - bestPrice) / retailPrice) * 100).toInt().coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = GamerSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("featured_hero_deal_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(game.bannerUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GamerBg.copy(alpha = 0.7f),
                                GamerBg.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPink,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "⭐ FEATURED DEAL",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GamerSurfaceHighlight.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                ) {
                    Text(
                        text = "-$savingsPercent% OFF",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = GamerTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "$currencySymbol${"%.2f".format(bestPrice)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = NeonGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$currencySymbol${"%.2f".format(retailPrice)}",
                        fontSize = 13.sp,
                        color = GamerTextMuted,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Metacritic: ${game.metacriticScore}",
                        fontSize = 12.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun GameItemCard(
    game: GameDetail,
    currencySymbol: String,
    exchangeRate: Double,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onClick: () -> Unit
) {
    val bestPrice = game.lowestHistoricalPriceUsd * exchangeRate
    val retailPrice = game.basePriceUsd * exchangeRate
    val discountPercent = (((retailPrice - bestPrice) / retailPrice) * 100).toInt().coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamerSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("game_card_${game.id}")
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Box Art Image
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .height(118.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(game.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (discountPercent > 0) {
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        color = NeonGreen,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-$discountPercent%",
                            color = GamerBg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GamerTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onWishlistToggle,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("wishlist_toggle_${game.id}")
                        ) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (isWishlisted) NeonPink else GamerTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Platforms Row
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        game.platforms.take(3).forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GamerSurfaceHighlight
                            ) {
                                Text(
                                    text = p.name,
                                    fontSize = 10.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Metacritic & Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GamerMetaBadge.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GamerMetaBadge)
                        ) {
                            Text(
                                text = "Meta ${game.metacriticScore}",
                                color = GamerTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "★ ${game.userRating}",
                            fontSize = 11.sp,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price and Store note
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Best Deal",
                            fontSize = 10.sp,
                            color = GamerTextMuted
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$currencySymbol${"%.2f".format(bestPrice)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = if (discountPercent > 0) NeonGreen else GamerTextPrimary
                            )
                            if (discountPercent > 0 && retailPrice > bestPrice) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$currencySymbol${"%.2f".format(retailPrice)}",
                                    fontSize = 12.sp,
                                    color = GamerTextMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
