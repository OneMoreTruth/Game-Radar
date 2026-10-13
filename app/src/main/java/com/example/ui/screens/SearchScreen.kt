package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.components.PreferredPlatformSelectorDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: GameViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val preferredPlatform by viewModel.preferredPlatform.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val games by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val wishlist by viewModel.wishlistItems.collectAsState()

    var showCountryDialog by remember { mutableStateOf(false) }
    var showPlatformDialog by remember { mutableStateOf(false) }

    val quickSearches = remember {
        listOf("Elden Ring", "Cyberpunk", "Zelda", "Spider-Man", "Black Myth", "Baldur's Gate", "Monster Hunter")
    }

    if (showCountryDialog) {
        CountrySelectorDialog(
            selectedCountry = selectedCountry,
            onCountrySelected = { viewModel.setCountry(it) },
            onDismiss = { showCountryDialog = false }
        )
    }

    if (showPlatformDialog) {
        PreferredPlatformSelectorDialog(
            preferredPlatform = preferredPlatform,
            onPlatformSelected = { platform, syncFilter ->
                viewModel.setPreferredPlatform(platform, updateActiveFilter = syncFilter)
            },
            onDismiss = { showPlatformDialog = false }
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
                // Header with Screen Title & Country Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonCyan,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Games",
                                    tint = GamerBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Search Games",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = GamerTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "PC, PlayStation, Xbox, Switch Deals",
                                fontSize = 11.sp,
                                color = GamerTextSecondary
                            )
                        }
                    }

                    // Country Pill Button
                    Surface(
                        onClick = { showCountryDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = GamerSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                        modifier = Modifier.testTag("search_country_picker_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedCountry.flagEmoji,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = selectedCountry.currencyCode,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Region",
                                tint = GamerTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Preferred Platform Pill Button
                    Surface(
                        onClick = { showPlatformDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = GamerSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                        modifier = Modifier.testTag("search_preferred_platform_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = when (preferredPlatform) {
                                    PlatformType.PC -> Icons.Default.DesktopWindows
                                    PlatformType.PS5 -> Icons.Default.Gamepad
                                    PlatformType.XBOX -> Icons.Default.VideogameAsset
                                    PlatformType.SWITCH -> Icons.Default.TabletAndroid
                                    PlatformType.ALL -> Icons.Default.SportsEsports
                                },
                                contentDescription = "Preferred Platform",
                                tint = NeonCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (preferredPlatform) {
                                    PlatformType.ALL -> "All"
                                    PlatformType.PC -> "PC"
                                    PlatformType.PS5 -> "PS5"
                                    PlatformType.XBOX -> "Xbox"
                                    PlatformType.SWITCH -> "Switch"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change preferred platform",
                                tint = GamerTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Interactive Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search by game title, genre, or studio...",
                            color = GamerTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = GamerTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
                        .testTag("search_bar_input")
                )

                // Quick Search Suggestions
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickSearches) { suggestion ->
                        val isCurrent = searchQuery.equals(suggestion, ignoreCase = true)
                        Surface(
                            onClick = { viewModel.setSearchQuery(suggestion) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) NeonCyan.copy(alpha = 0.2f) else GamerSurfaceElevated,
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                            modifier = Modifier.testTag("quick_search_$suggestion")
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) NeonCyan else GamerTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Platform Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PlatformType.values()) { platform ->
                        val isSelected = selectedPlatform == platform
                        val isPreferred = platform == preferredPlatform && platform != PlatformType.ALL
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setPlatform(platform) },
                            leadingIcon = if (isPreferred) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Preferred",
                                        tint = if (isSelected) GamerBg else NeonAmber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else null,
                            label = {
                                Text(
                                    text = if (isPreferred) "${platform.displayName} ★" else platform.displayName,
                                    fontSize = 11.sp,
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
                            modifier = Modifier.testTag("search_platform_chip_${platform.name}")
                        )
                    }
                }

                // Sort Options Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Sort:",
                        fontSize = 11.sp,
                        color = GamerTextMuted,
                        modifier = Modifier.padding(end = 6.dp)
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
                                modifier = Modifier.testTag("search_sort_chip_${sort.name}")
                            ) {
                                Text(
                                    text = sort.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonPurple else GamerTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
        // Lazy List displaying search results
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_search_results_lazy_list")
        ) {
            // Results Count Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank())
                            "Found ${games.size} results for \"$searchQuery\""
                        else
                            "Trending & Recommended Games (${games.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Region: ${selectedCountry.name}",
                        fontSize = 11.sp,
                        color = GamerTextMuted
                    )
                }
            }

            // Empty state
            if (games.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GamerSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No Results",
                                tint = GamerTextMuted,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No games found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for another console or PC title, or reset your platform filter.",
                                fontSize = 12.sp,
                                color = GamerTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.setSearchQuery("")
                                    viewModel.setPlatform(PlatformType.ALL)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = GamerBg
                                )
                            ) {
                                Text("Reset Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                val effectivePlatform = if (selectedPlatform != PlatformType.ALL) selectedPlatform else preferredPlatform
                // Game Search Result Cards in Lazy List
                items(games, key = { it.id }) { game ->
                    val isWishlisted = wishlist.any { it.gameId == game.id }
                    SearchResultCard(
                        game = game,
                        selectedCountry = selectedCountry,
                        targetPlatform = effectivePlatform,
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
private fun SearchResultCard(
    game: GameDetail,
    selectedCountry: com.example.data.model.CountryInfo,
    targetPlatform: PlatformType = PlatformType.ALL,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onClick: () -> Unit
) {
    val currencySymbol = selectedCountry.currencySymbol
    val bestPrice = game.getDisplayLowestPrice(selectedCountry, targetPlatform)
    val retailPrice = game.getDisplayBasePrice(selectedCountry, targetPlatform)
    val discountPercent = if (retailPrice > 0) (((retailPrice - bestPrice) / retailPrice) * 100).toInt().coerceAtLeast(0) else 0

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamerSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search_game_card_${game.id}")
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Box Art Poster with Discount Badge
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .height(116.dp)
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
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 116.dp),
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
                                .testTag("search_wishlist_toggle_${game.id}")
                        ) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (isWishlisted) NeonPink else GamerTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Developer & Publisher note
                    if (game.developer.isNotBlank()) {
                        Text(
                            text = game.developer,
                            fontSize = 11.sp,
                            color = GamerTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Platform badges
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        game.platforms.take(3).forEach { platform ->
                            val isCurrent = platform == targetPlatform
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isCurrent) NeonCyan.copy(alpha = 0.25f) else GamerSurfaceHighlight,
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null
                            ) {
                                Text(
                                    text = platform.name,
                                    fontSize = 9.sp,
                                    color = if (isCurrent) NeonCyan else GamerTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Metacritic and User Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
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

                // Price and Store link
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (targetPlatform != PlatformType.ALL) "${targetPlatform.displayName} Deal" else "Best Available Deal",
                                fontSize = 10.sp,
                                color = GamerTextMuted
                            )
                            if (targetPlatform != PlatformType.ALL) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = NeonCyan.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonCyan)
                                ) {
                                    Text(
                                        text = targetPlatform.name,
                                        color = NeonCyan,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                    )
                                }
                            }
                        }
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

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonCyan.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "View Deals",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Deals",
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
