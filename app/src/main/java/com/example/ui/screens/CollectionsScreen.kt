package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    viewModel: GameViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allCollections by viewModel.allCollections.collectAsState()
    val activeFilter by viewModel.selectedCollectionFilter.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()

    val filterTabs = listOf(
        "ALL" to "All Games",
        "PLAYING" to "🎮 Playing",
        "BACKLOG" to "📚 Backlog",
        "COMPLETED" to "🏆 Completed",
        "FAVORITE" to "⭐ Favorites"
    )

    val displayedItems = if (activeFilter == "ALL") {
        allCollections
    } else {
        allCollections.filter { it.collectionType == activeFilter }
    }

    val totalCount = allCollections.size
    val standardRegionalPrice = com.example.data.repository.CountryPricingManager.getRegionalBasePrice(59.99, selectedCountry.code)
    val estimatedValue = totalCount * standardRegionalPrice
    val estimatedSavings = totalCount * (standardRegionalPrice * 0.35)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CollectionsBookmark,
                            contentDescription = "Collections",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "My Game Collections",
                            fontWeight = FontWeight.Bold,
                            color = GamerTextPrimary,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    // Social Share Button
                    Button(
                        onClick = {
                            val shareContent = viewModel.buildShareCollectionText(activeFilter)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareContent)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Collection to Gamers"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPurple,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("share_collection_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Collection",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GamerBg,
                    titleContentColor = GamerTextPrimary
                )
            )
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
            // Stats & Social Banner
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = GamerSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Gamer Profile & Library Stats",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonCyan.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                            ) {
                                Text(
                                    text = "$totalCount GAMES LOGGED",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Library Est. Value", fontSize = 11.sp, color = GamerTextMuted)
                                Text(
                                    text = "${selectedCountry.currencySymbol}${"%.2f".format(estimatedValue)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = GamerTextPrimary
                                )
                            }
                            Column {
                                Text("Deal Money Saved", fontSize = 11.sp, color = GamerTextMuted)
                                Text(
                                    text = "${selectedCountry.currencySymbol}${"%.2f".format(estimatedSavings)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = NeonGreen
                                )
                            }
                            Column {
                                Text("Active Region", fontSize = 11.sp, color = GamerTextMuted)
                                Text(
                                    text = "${selectedCountry.flagEmoji} ${selectedCountry.currencyCode}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NeonAmber
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Social quick share bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GamerSurfaceHighlight)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "Social Integration",
                                tint = NeonPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share to Discord, Reddit, or WhatsApp",
                                fontSize = 12.sp,
                                color = GamerTextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val text = viewModel.buildShareCollectionText(activeFilter)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("GameRadar Collection", text))
                                    Toast.makeText(context, "Collection Markdown copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy markdown",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Collection Categories Filter Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTabs) { (key, label) ->
                        val isSelected = activeFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCollectionFilter(key) },
                            label = {
                                Text(
                                    text = label,
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
                            modifier = Modifier.testTag("collection_tab_$key")
                        )
                    }
                }
            }

            if (displayedItems.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Empty",
                                tint = GamerTextMuted,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No games in this collection yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary
                            )
                            Text(
                                text = "Browse games and tap 'Library' on any game to categorize it.",
                                fontSize = 13.sp,
                                color = GamerTextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(displayedItems, key = { it.id }) { item ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GamerSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(item.gameId) }
                            .testTag("collection_item_${item.id}")
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.posterUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(98.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        color = GamerTextPrimary,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = { viewModel.toggleGameCollection(
                                            com.example.data.model.GameDetail(
                                                id = item.gameId,
                                                title = item.title,
                                                bannerUrl = item.posterUrl,
                                                posterUrl = item.posterUrl,
                                                platforms = emptyList(),
                                                developer = "",
                                                publisher = "",
                                                releaseDate = "",
                                                metacriticScore = 80,
                                                userRating = item.rating,
                                                esrbRating = "",
                                                genres = emptyList(),
                                                description = "",
                                                trailerYoutubeId = "",
                                                trailerTitle = "",
                                                screenshots = emptyList(),
                                                basePriceUsd = 59.99,
                                                lowestHistoricalPriceUsd = 39.99,
                                                systemRequirements = null
                                            ),
                                            item.collectionType
                                        ) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove from category",
                                            tint = GamerTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    val typeColor = when (item.collectionType) {
                                        "PLAYING" -> NeonGreen
                                        "BACKLOG" -> NeonAmber
                                        "COMPLETED" -> NeonPurple
                                        else -> NeonPink
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = typeColor.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor)
                                    ) {
                                        Text(
                                            text = item.collectionType,
                                            color = typeColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.platform,
                                        fontSize = 11.sp,
                                        color = GamerTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "★ ${item.rating}/5.0",
                                        fontSize = 12.sp,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = "View Deals",
                                        fontSize = 11.sp,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
