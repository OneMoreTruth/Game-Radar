package com.example.ui.screens

import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.CopyType
import com.example.data.model.GameDetail
import com.example.notification.NotificationHelper
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

enum class DetailSectionTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    WHERE_TO_BUY("Where to Buy", Icons.Default.ShoppingCart),
    TRAILERS("Trailers & Media", Icons.Default.Movie),
    SPECS("System Specs", Icons.Default.Memory),
    REVIEWS("User Reviews", Icons.Default.RateReview)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: String,
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(gameId) {
        viewModel.loadGameDetail(gameId)
    }

    BackHandler {
        onNavigateBack()
    }

    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val game by viewModel.selectedGame.collectAsState()
    val reviews by viewModel.selectedGameReviews.collectAsState()
    val isInWishlist by viewModel.isInWishlist.collectAsState()
    val activePurchaseTab by viewModel.activePurchaseTab.collectAsState()
    val collectionTypes by viewModel.gameCollectionTypes.collectAsState()

    var activeSection by remember { mutableStateOf(DetailSectionTab.WHERE_TO_BUY) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showTargetPriceDialog by remember { mutableStateOf(false) }
    var showCollectionMenu by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission handled */ }

    if (showCountryDialog) {
        CountrySelectorDialog(
            selectedCountry = selectedCountry,
            onCountrySelected = { viewModel.setCountry(it) },
            onDismiss = { showCountryDialog = false }
        )
    }

    val currentGame = game

    if (showReviewDialog && currentGame != null) {
        WriteReviewDialog(
            gameTitle = currentGame.title,
            onSubmit = { name, rating, platform, hours, text, pros, cons ->
                viewModel.submitUserReview(currentGame.id, name, rating, platform, hours, text, pros, cons)
            },
            onDismiss = { showReviewDialog = false }
        )
    }

    if (showTargetPriceDialog && currentGame != null) {
        val currentBest = currentGame.lowestHistoricalPriceUsd * selectedCountry.exchangeRateToUsd
        TargetPriceDialog(
            gameTitle = currentGame.title,
            currentPrice = currentBest,
            currencySymbol = selectedCountry.currencySymbol,
            initialTarget = currentBest * 0.85,
            onSaveTarget = { newTarget, notes, priority ->
                viewModel.toggleWishlist(currentGame, newTarget, notes, priority)
            },
            onDismiss = { showTargetPriceDialog = false }
        )
    }

    if (currentGame == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GamerBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NeonCyan)
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentGame.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GamerTextPrimary
                        )
                    }
                },
                actions = {
                    // Notification Icon for Price Drop Alerts (mock simulation)
                    IconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !NotificationHelper.hasNotificationPermission(context)
                            ) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.triggerPriceDropTest(currentGame.id) { oldPrice, newPrice ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "🔔 Price Drop Alert: Price decreased from ${selectedCountry.currencySymbol}${"%.2f".format(oldPrice)} to ${selectedCountry.currencySymbol}${"%.2f".format(newPrice)} (-25%)! Notification sent.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("price_drop_notification_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Simulate Price Drop Alert",
                            tint = NeonGreen
                        )
                    }

                    // Country pill
                    Surface(
                        onClick = { showCountryDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = GamerSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(selectedCountry.flagEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                selectedCountry.currencyCode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary
                            )
                        }
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareText = viewModel.buildShareGameText(currentGame)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share ${currentGame.title}"))
                        },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Game",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GamerBg.copy(alpha = 0.95f),
                    titleContentColor = GamerTextPrimary
                )
            )
        },
        bottomBar = {
            // Persistent Bottom Action Bar
            Surface(
                color = GamerSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Best Price in ${selectedCountry.name}",
                            fontSize = 11.sp,
                            color = GamerTextMuted
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            val best = currentGame.lowestHistoricalPriceUsd * selectedCountry.exchangeRateToUsd
                            val orig = currentGame.basePriceUsd * selectedCountry.exchangeRateToUsd
                            Text(
                                text = "${selectedCountry.currencySymbol}${"%.2f".format(best)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = NeonGreen
                            )
                            if (orig > best) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${selectedCountry.currencySymbol}${"%.2f".format(orig)}",
                                    fontSize = 12.sp,
                                    color = GamerTextMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }

                    // Wishlist Button
                    FilledTonalButton(
                        onClick = {
                            if (!isInWishlist) {
                                showTargetPriceDialog = true
                            } else {
                                viewModel.toggleWishlist(currentGame)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isInWishlist) NeonPink.copy(alpha = 0.2f) else GamerSurfaceHighlight,
                            contentColor = if (isInWishlist) NeonPink else GamerTextPrimary
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("wishlist_detail_action")
                    ) {
                        Icon(
                            imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isInWishlist) "Saved" else "Wishlist",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Collections Dropdown Button
                    Box {
                        Button(
                            onClick = { showCollectionMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = GamerBg
                            ),
                            modifier = Modifier.testTag("add_to_collection_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Collection",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Library",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        DropdownMenu(
                            expanded = showCollectionMenu,
                            onDismissRequest = { showCollectionMenu = false },
                            modifier = Modifier
                                .background(GamerSurfaceElevated)
                                .border(1.dp, GamerBorder, RoundedCornerShape(8.dp))
                        ) {
                            listOf(
                                "PLAYING" to "🎮 Currently Playing",
                                "BACKLOG" to "📚 Backlog",
                                "COMPLETED" to "🏆 Completed",
                                "FAVORITE" to "⭐ Favorite"
                            ).forEach { (type, label) ->
                                val inThis = collectionTypes.contains(type)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = label,
                                                color = if (inThis) NeonCyan else GamerTextPrimary,
                                                fontWeight = if (inThis) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (inThis) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "In collection",
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.toggleGameCollection(currentGame, type)
                                        showCollectionMenu = false
                                    }
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
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Banner & Box Art Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentGame.bannerUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = currentGame.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GamerBg.copy(alpha = 0.6f),
                                        GamerBg.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    // Box Art + Overlaid Details
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentGame.posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = currentGame.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(80.dp)
                                .height(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(10.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            // Platforms
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                currentGame.platforms.forEach { p ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = GamerSurfaceHighlight
                                    ) {
                                        Text(
                                            text = p.name,
                                            fontSize = 9.sp,
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentGame.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = GamerTextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GamerMetaBadge,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "Metacritic ${currentGame.metacriticScore}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "★ ${currentGame.userRating} User Score",
                                    fontSize = 11.sp,
                                    color = NeonAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Info Chips: Developer, Publisher, Release Date, ESRB
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GamerSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Developer", fontSize = 10.sp, color = GamerTextMuted)
                                Text(currentGame.developer, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GamerTextPrimary)
                            }
                            Column {
                                Text("Publisher", fontSize = 10.sp, color = GamerTextMuted)
                                Text(currentGame.publisher, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GamerTextPrimary)
                            }
                            Column {
                                Text("Released", fontSize = 10.sp, color = GamerTextMuted)
                                Text(currentGame.releaseDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GamerTextPrimary)
                            }
                            Column {
                                Text("Rating", fontSize = 10.sp, color = GamerTextMuted)
                                Text(currentGame.esrbRating, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
                            }
                        }

                        // Genres
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            currentGame.genres.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GamerSurfaceHighlight
                                ) {
                                    Text(
                                        text = genre,
                                        fontSize = 11.sp,
                                        color = GamerTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Push Notification Simulator / Alert Triggers card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GamerSurfaceHighlight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alerts",
                                tint = NeonPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Push Notification Alerts",
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "Test real Android notifications for price drops and release alerts:",
                            fontSize = 11.sp,
                            color = GamerTextSecondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !NotificationHelper.hasNotificationPermission(context)
                                    ) {
                                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    viewModel.triggerPriceDropTest(currentGame.id) { oldPrice, newPrice ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "🔔 Price Drop Alert: Price decreased from ${selectedCountry.currencySymbol}${"%.2f".format(oldPrice)} to ${selectedCountry.currencySymbol}${"%.2f".format(newPrice)} (-25%)! Notification sent.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_price_drop_notification_button")
                            ) {
                                Text("🔔 Test Price Drop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !NotificationHelper.hasNotificationPermission(context)
                                    ) {
                                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    viewModel.triggerReleaseAlertTest(currentGame.id)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "🚀 Launch Alert simulated for ${currentGame.title}!",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_release_alert_notification_button")
                            ) {
                                Text("🚀 Test Release Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Reset price button
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = {
                                viewModel.resetPriceTest(currentGame.id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "↺ Baseline price restored for ${currentGame.title}",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("reset_price_button")
                        ) {
                            Text(
                                "↺ Reset to Original Price",
                                fontSize = 11.sp,
                                color = GamerTextMuted
                            )
                        }
                    }
                }
            }

            // Navigation Tabs (Where to Buy, Trailers & Media, Specs, Reviews)
            item {
                ScrollableTabRow(
                    selectedTabIndex = activeSection.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = NeonCyan,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeSection.ordinal]),
                            color = NeonCyan,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DetailSectionTab.values().forEach { tab ->
                        val isSelected = activeSection == tab
                        Tab(
                            selected = isSelected,
                            onClick = { activeSection = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonCyan else GamerTextSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) NeonCyan else GamerTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.testTag("tab_${tab.name}")
                        )
                    }
                }
            }

            // Tab Content
            when (activeSection) {
                DetailSectionTab.WHERE_TO_BUY -> {
                    item {
                        Column {
                            // Sub-tabs: Physical Copies vs Digital Copies
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GamerSurfaceElevated)
                                    .padding(4.dp)
                            ) {
                                val isDigital = activePurchaseTab == CopyType.DIGITAL
                                Surface(
                                    onClick = { viewModel.setActivePurchaseTab(CopyType.DIGITAL) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDigital) NeonCyan else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tab_digital_copies")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = "Digital",
                                            tint = if (isDigital) GamerBg else GamerTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Digital Copies (${currentGame.digitalOffers.size})",
                                            fontWeight = if (isDigital) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isDigital) GamerBg else GamerTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                val isPhysical = activePurchaseTab == CopyType.PHYSICAL
                                Surface(
                                    onClick = { viewModel.setActivePurchaseTab(CopyType.PHYSICAL) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isPhysical) NeonCyan else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tab_physical_copies")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Album,
                                            contentDescription = "Physical",
                                            tint = if (isPhysical) GamerBg else GamerTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Physical Copies (${currentGame.physicalOffers.size})",
                                            fontWeight = if (isPhysical) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPhysical) GamerBg else GamerTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Region note
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Stores in ${selectedCountry.name} ${selectedCountry.flagEmoji}",
                                    fontSize = 12.sp,
                                    color = GamerTextMuted
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Prices in ${selectedCountry.currencyCode}",
                                    fontSize = 12.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    val currentOffers = if (activePurchaseTab == CopyType.DIGITAL) {
                        currentGame.digitalOffers
                    } else {
                        currentGame.physicalOffers
                    }

                    if (currentOffers.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = GamerSurfaceElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = "No offers currently indexed for this copy type in ${selectedCountry.name}.",
                                        color = GamerTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(currentOffers, key = { it.id }) { offer ->
                            StoreOfferRow(
                                offer = offer,
                                currencySymbol = selectedCountry.currencySymbol
                            )
                        }
                    }
                }

                DetailSectionTab.TRAILERS -> {
                    item {
                        TrailerPlayerPreview(
                            youtubeVideoId = currentGame.trailerYoutubeId,
                            title = currentGame.trailerTitle
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GamerSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Game Synopsis",
                                    fontWeight = FontWeight.Bold,
                                    color = GamerTextPrimary,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentGame.description,
                                    color = GamerTextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    if (currentGame.screenshots.isNotEmpty()) {
                        item {
                            Text(
                                text = "Gameplay Screenshots",
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(currentGame.screenshots) { screenshotUrl ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(screenshotUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Screenshot",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(260.dp)
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, GamerBorder, RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                DetailSectionTab.SPECS -> {
                    item {
                        SystemRequirementsSection(requirements = currentGame.systemRequirements)
                    }
                }

                DetailSectionTab.REVIEWS -> {
                    item {
                        // Rating Header & Write Review Action
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GamerSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Community Rating",
                                        fontSize = 12.sp,
                                        color = GamerTextMuted
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "${currentGame.userRating}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 28.sp,
                                            color = NeonAmber
                                        )
                                        Text(
                                            text = " / 5.0",
                                            fontSize = 14.sp,
                                            color = GamerTextSecondary,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = "${reviews.size} gamer reviews logged",
                                        fontSize = 11.sp,
                                        color = GamerTextSecondary
                                    )
                                }

                                Button(
                                    onClick = { showReviewDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonPurple,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("open_write_review_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Write Review",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Write Review", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    if (reviews.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = GamerSurfaceElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "Be the first gamer to write a review for ${currentGame.title}!",
                                        color = GamerTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(reviews, key = { it.id }) { review ->
                            UserReviewItemCard(review = review)
                        }
                    }
                }
            }
        }
    }
}
