package com.example.ui.screens

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
import com.example.data.local.WishlistItemEntity
import com.example.ui.components.TargetPriceDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: GameViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val wishlist by viewModel.wishlistItems.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPriorityFilter by remember { mutableStateOf("ALL") }
    var editingGameItem by remember { mutableStateOf<WishlistItemEntity?>(null) }

    val filteredList = remember(wishlist, searchQuery, selectedPriorityFilter) {
        wishlist.filter { item ->
            val matchesQuery = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true)
            val matchesPriority = selectedPriorityFilter == "ALL" || item.priority == selectedPriorityFilter
            matchesQuery && matchesPriority
        }
    }

    if (editingGameItem != null) {
        val item = editingGameItem!!
        TargetPriceDialog(
            gameTitle = item.title,
            currentPrice = item.currentPrice,
            currencySymbol = selectedCountry.currencySymbol,
            initialTarget = item.targetPrice,
            initialNotes = item.notes,
            initialPriority = item.priority,
            onSaveTarget = { newTarget, notes, priority ->
                viewModel.updateWishlistTargetPrice(item.gameId, newTarget)
                viewModel.updateWishlistNotes(item.gameId, notes)
                viewModel.updateWishlistPriority(item.gameId, priority)
                editingGameItem = null
            },
            onDismiss = { editingGameItem = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Wishlist",
                            tint = NeonPink,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Wishlist & Price Alerts",
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary,
                                fontSize = 18.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = NeonGreen,
                                    modifier = Modifier.size(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Room SQLite • Offline Available",
                                    fontSize = 11.sp,
                                    color = NeonGreen
                                )
                            }
                        }
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
            // Header stats banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GamerSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Offline-First Room Persistence",
                                    fontWeight = FontWeight.Bold,
                                    color = GamerTextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Tracked games, custom alert thresholds, and notes stay available offline without network.",
                                    fontSize = 12.sp,
                                    color = GamerTextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonGreen.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Offline Cache",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${wishlist.size} SAVED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // In-wishlist Search & Priority Filter row
            if (wishlist.isNotEmpty()) {
                item {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Filter wishlist...", fontSize = 13.sp, color = GamerTextMuted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search wishlist",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = GamerTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GamerSurfaceElevated,
                                unfocusedContainerColor = GamerSurfaceElevated,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GamerBorder,
                                focusedTextColor = GamerTextPrimary,
                                unfocusedTextColor = GamerTextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("filter_wishlist_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "ALL" to "All (${wishlist.size})",
                                "HIGH" to "🔥 High Priority",
                                "MEDIUM" to "⚡ Medium",
                                "LOW" to "⏳ Low Priority"
                            ).forEach { (key, label) ->
                                val selected = selectedPriorityFilter == key
                                item {
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedPriorityFilter = key },
                                        label = {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
                                            selected = selected,
                                            borderColor = if (selected) NeonCyan else GamerBorder,
                                            selectedBorderColor = NeonCyan
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Empty Wishlist",
                                tint = GamerTextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (wishlist.isEmpty()) "Your offline wishlist is empty" else "No matching games found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary
                            )
                            Text(
                                text = if (wishlist.isEmpty())
                                    "Tap the heart on any game to track price drops & release alerts offline."
                                else
                                    "Try clearing your search or selecting a different priority filter.",
                                fontSize = 13.sp,
                                color = GamerTextSecondary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.gameId }) { item ->
                    val isTargetHit = item.currentPrice <= item.targetPrice
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GamerSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isTargetHit) NeonGreen else GamerBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(item.gameId) }
                            .testTag("wishlist_item_${item.gameId}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row {
                                // Poster
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.posterUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(76.dp)
                                        .height(104.dp)
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
                                            onClick = {
                                                viewModel.removeFromWishlist(item.gameId)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = GamerTextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Priority badge & Platform
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        val priorityColor = when (item.priority) {
                                            "HIGH" -> NeonPink
                                            "LOW" -> NeonCyan
                                            else -> NeonAmber
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = priorityColor.copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, priorityColor)
                                        ) {
                                            Text(
                                                text = "${item.priority} PRIORITY",
                                                color = priorityColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.platforms,
                                            fontSize = 11.sp,
                                            color = GamerTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(
                                                text = "Current: ${selectedCountry.currencySymbol}${"%.2f".format(item.currentPrice)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTargetHit) NeonGreen else GamerTextPrimary
                                            )
                                            Text(
                                                text = "Target Alert: ${selectedCountry.currencySymbol}${"%.2f".format(item.targetPrice)}",
                                                fontSize = 11.sp,
                                                color = NeonAmber
                                            )
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        // Edit target button
                                        OutlinedButton(
                                            onClick = { editingGameItem = item },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EditNotifications,
                                                contentDescription = "Edit Alert",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // Personal Offline Note if present
                            if (item.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GamerSurfaceHighlight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Note,
                                            contentDescription = "Note",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.notes,
                                            fontSize = 11.sp,
                                            color = GamerTextSecondary
                                        )
                                    }
                                }
                            }

                            // Quick test notification trigger
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.triggerPriceDropTest(item.gameId) },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text(
                                        text = "🔔 Push Alert Test",
                                        fontSize = 11.sp,
                                        color = NeonGreen,
                                        fontWeight = FontWeight.SemiBold
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
