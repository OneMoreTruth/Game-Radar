package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notification.NotificationHelper
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val alerts by viewModel.alertHistory.collectAsState()
    val games by viewModel.searchResults.collectAsState()

    var hasPermission by remember {
        mutableStateOf(NotificationHelper.hasNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Push Alert Center",
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("alerts_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GamerTextPrimary
                        )
                    }
                },
                actions = {
                    if (alerts.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearAlerts() },
                            modifier = Modifier.testTag("clear_alerts_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Alerts",
                                tint = GamerTextMuted
                            )
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
                bottom = innerPadding.calculateBottomPadding() + 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Permission card if not granted on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NeonPink.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = "Permission needed",
                                    tint = NeonPink,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Enable Push Notifications",
                                    fontWeight = FontWeight.Bold,
                                    color = GamerTextPrimary,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "Grant permission to receive instant alerts when your wishlisted games drop in price or launch.",
                                fontSize = 12.sp,
                                color = GamerTextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonPink,
                                    contentColor = GamerBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Allow Notifications", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Interactive Test Triggers Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GamerSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = "Triggers",
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Push Notification Simulation",
                                fontWeight = FontWeight.Bold,
                                color = GamerTextPrimary,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            text = "Test real Android status bar push notifications for price drops and upcoming launch alerts:",
                            fontSize = 12.sp,
                            color = GamerTextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val targetGameId = games.firstOrNull()?.id ?: "elden-ring"

                            Button(
                                onClick = {
                                    viewModel.triggerPriceDropTest(targetGameId)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGreen,
                                    contentColor = GamerBg
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("trigger_test_price_drop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = "Price Drop",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Price Drop Alert",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.triggerReleaseAlertTest(targetGameId)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = GamerBg
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("trigger_test_release_alert_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = "Release Alert",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Release Alert",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Notification Channels Info
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Alerts History & Logs (${alerts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                }
            }

            if (alerts.isEmpty()) {
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
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "No push alerts triggered yet. Tap the test buttons above or add games to your wishlist!",
                                color = GamerTextSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(alerts, key = { it.id }) { alert ->
                    val isPriceDrop = alert.alertType == "PRICE_DROP"
                    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    val formattedTime = timeFormat.format(Date(alert.timestamp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GamerSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPriceDrop) NeonGreen.copy(alpha = 0.4f) else NeonCyan.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alert_item_${alert.id}")
                    ) {
                        Row(modifier = Modifier.padding(14.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = if (isPriceDrop) NeonGreen.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPriceDrop) Icons.Default.TrendingDown else Icons.Default.RocketLaunch,
                                        contentDescription = alert.alertType,
                                        tint = if (isPriceDrop) NeonGreen else NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = alert.gameTitle,
                                        fontWeight = FontWeight.Bold,
                                        color = GamerTextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = formattedTime,
                                        fontSize = 11.sp,
                                        color = GamerTextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.message,
                                    fontSize = 12.sp,
                                    color = GamerTextSecondary,
                                    lineHeight = 17.sp
                                )

                                if (alert.storeName.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Store: ${alert.storeName}",
                                        fontSize = 11.sp,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Medium
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
