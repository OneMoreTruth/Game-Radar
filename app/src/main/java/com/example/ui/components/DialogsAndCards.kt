package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CountryInfo
import com.example.data.model.PlatformType
import com.example.data.model.StoreOffer
import com.example.ui.theme.*

@Composable
fun CountrySelectorDialog(
    selectedCountry: CountryInfo,
    onCountrySelected: (CountryInfo) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GamerSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("country_selector_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Country",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Select Store Region",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                }
                Text(
                    text = "Updates physical retailers, digital stores, and currencies in real-time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GamerTextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                CountryInfo.ALL_COUNTRIES.forEach { country ->
                    val isSelected = country.code == selectedCountry.code
                    Surface(
                        onClick = {
                            onCountrySelected(country)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GamerSurfaceHighlight else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("country_option_${country.code}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = country.flagEmoji,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = country.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = GamerTextPrimary
                                )
                                Text(
                                    text = "Currency: ${country.currencyCode} (${country.currencySymbol})",
                                    fontSize = 12.sp,
                                    color = GamerTextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = GamerTextSecondary)
                }
            }
        }
    }
}

@Composable
fun PreferredPlatformSelectorDialog(
    preferredPlatform: PlatformType,
    onPlatformSelected: (PlatformType, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var syncActiveFilter by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GamerSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("preferred_platform_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Preferred Platform",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Preferred Platform",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                }
                Text(
                    text = "Select your preferred console or PC. GameRadar will prioritize and filter prices, deals, and store offers to match.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GamerTextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                PlatformType.values().forEach { platform ->
                    val isSelected = platform == preferredPlatform
                    val iconVector = when (platform) {
                        PlatformType.ALL -> Icons.Default.SportsEsports
                        PlatformType.PC -> Icons.Default.DesktopWindows
                        PlatformType.PS5 -> Icons.Default.Gamepad
                        PlatformType.XBOX -> Icons.Default.VideogameAsset
                        PlatformType.SWITCH -> Icons.Default.TabletAndroid
                    }
                    val subtitle = when (platform) {
                        PlatformType.ALL -> "Compare across all consoles and PC storefronts"
                        PlatformType.PC -> "Steam, Epic Games, CheapShark & PC Keys"
                        PlatformType.PS5 -> "PlayStation Store, Loaded PS5 keys & Boxed Discs"
                        PlatformType.XBOX -> "Xbox Games Store, Loaded keys & Xbox Discs"
                        PlatformType.SWITCH -> "Nintendo eShop & Switch Cartridges"
                    }

                    Surface(
                        onClick = {
                            onPlatformSelected(platform, syncActiveFilter)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GamerSurfaceHighlight else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("preferred_platform_option_${platform.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = platform.displayName,
                                tint = if (isSelected) NeonCyan else GamerTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = platform.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonCyan else GamerTextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = subtitle,
                                    fontSize = 11.sp,
                                    color = GamerTextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syncActiveFilter = !syncActiveFilter }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = syncActiveFilter,
                        onCheckedChange = { syncActiveFilter = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            checkmarkColor = GamerBg
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Also filter active search/home to this console",
                        fontSize = 12.sp,
                        color = GamerTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = GamerTextSecondary)
                }
            }
        }
    }
}

@Composable
fun StoreOfferRow(
    offer: StoreOffer,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val isLoadedStore = offer.storeName.contains("Loaded", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GamerSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLoadedStore) NeonPink else if (offer.discountPercent > 20) NeonGreen.copy(alpha = 0.5f) else GamerBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("store_offer_${offer.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            if (isLoadedStore) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Loaded.com",
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${"%.2f".format(offer.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(offer.purchaseUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPink,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("buy_now_button_${offer.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Direct Link",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Direct link to game page",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = offer.storeName,
                            fontWeight = FontWeight.Bold,
                            color = GamerTextPrimary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (offer.discountPercent > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonGreen.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                            ) {
                                Text(
                                    text = "-${offer.discountPercent}%",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GamerSurfaceHighlight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder)
                        ) {
                            Text(
                                text = offer.platform.displayName,
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = offer.edition,
                            fontSize = 12.sp,
                            color = GamerTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = " • ",
                            fontSize = 12.sp,
                            color = GamerTextMuted
                        )
                        Text(
                            text = offer.condition,
                            fontSize = 11.sp,
                            color = if (offer.condition.contains("Pre-Owned")) NeonAmber else NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currencySymbol${"%.2f".format(offer.price)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = if (offer.discountPercent > 0) NeonGreen else GamerTextPrimary
                        )
                        if (offer.discountPercent > 0 && offer.originalPrice > offer.price) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$currencySymbol${"%.2f".format(offer.originalPrice)}",
                                fontSize = 13.sp,
                                color = GamerTextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(offer.purchaseUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = GamerBg
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("buy_now_button_${offer.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Buy Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Store",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WriteReviewDialog(
    gameTitle: String,
    onSubmit: (reviewerName: String, rating: Float, platform: String, hours: Int, text: String, pros: List<String>, cons: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var reviewerName by remember { mutableStateOf("") }
    var rating by remember { mutableFloatStateOf(5.0f) }
    var platform by remember { mutableStateOf("PC / Steam") }
    var hours by remember { mutableStateOf("40") }
    var reviewText by remember { mutableStateOf("") }
    var prosText by remember { mutableStateOf("") }
    var consText by remember { mutableStateOf("") }

    val platformOptions = listOf("PC / Steam", "PlayStation 5", "Xbox Series X|S", "Nintendo Switch")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GamerSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("write_review_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Review $gameTitle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GamerTextPrimary
                )
                Text(
                    text = "Share your verified impressions with fellow gamers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GamerTextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Rating stars selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Text("Rating: ", color = GamerTextPrimary, fontWeight = FontWeight.Medium)
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { rating = star.toFloat() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$star stars",
                                tint = NeonAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = "★ $rating/5.0",
                        color = NeonAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text("Gamer Tag / Username") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GamerBorder,
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("review_gamertag_input")
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("Hours Played") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GamerBorder,
                            focusedTextColor = GamerTextPrimary,
                            unfocusedTextColor = GamerTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                            .testTag("review_hours_input")
                    )

                    OutlinedTextField(
                        value = platform,
                        onValueChange = { platform = it },
                        label = { Text("Platform") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GamerBorder,
                            focusedTextColor = GamerTextPrimary,
                            unfocusedTextColor = GamerTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .padding(start = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Detailed Review") },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GamerBorder,
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("review_text_input")
                )

                OutlinedTextField(
                    value = prosText,
                    onValueChange = { prosText = it },
                    label = { Text("Pros (comma separated)") },
                    placeholder = { Text("e.g. Combat, Graphics, Story") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = GamerBorder,
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )

                OutlinedTextField(
                    value = consText,
                    onValueChange = { consText = it },
                    label = { Text("Cons (comma separated)") },
                    placeholder = { Text("e.g. Framerate dips, Grindy") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = GamerBorder,
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val name = if (reviewerName.isBlank()) "Gamer_${(100..999).random()}" else reviewerName
                            val prosList = prosText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val consList = consText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val hoursInt = hours.toIntOrNull() ?: 10
                            onSubmit(name, rating, platform, hoursInt, reviewText.ifBlank { "Great gameplay experience!" }, prosList, consList)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = GamerBg),
                        modifier = Modifier.testTag("submit_review_button")
                    ) {
                        Text("Post Review", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TargetPriceDialog(
    gameTitle: String,
    currentPrice: Double,
    currencySymbol: String,
    initialTarget: Double,
    initialNotes: String = "",
    initialPriority: String = "MEDIUM",
    onSaveTarget: (targetPrice: Double, notes: String, priority: String) -> Unit,
    onDismiss: () -> Unit
) {
    var targetInput by remember { mutableStateOf("%.2f".format(initialTarget)) }
    var notesInput by remember { mutableStateOf(initialNotes) }
    var priorityInput by remember { mutableStateOf(initialPriority) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GamerSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("target_price_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Price Alert",
                        tint = NeonAmber,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set Price Drop Alert",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Track: $gameTitle",
                    color = GamerTextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Current Best Price: $currencySymbol${"%.2f".format(currentPrice)}",
                    color = NeonGreen,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Target Price Alert Threshold:",
                    fontSize = 12.sp,
                    color = GamerTextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it },
                    prefix = { Text(currencySymbol, color = NeonCyan, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GamerBorder,
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_price_input")
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Wishlist Priority:",
                    fontSize = 12.sp,
                    color = GamerTextSecondary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("HIGH" to "🔥 High", "MEDIUM" to "⚡ Medium", "LOW" to "⏳ Low").forEach { (key, label) ->
                        val selected = priorityInput == key
                        Surface(
                            onClick = { priorityInput = key },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) NeonCyan.copy(alpha = 0.2f) else GamerSurfaceHighlight,
                            border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) NeonCyan else GamerTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Offline Personal Note:",
                    fontSize = 12.sp,
                    color = GamerTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    placeholder = { Text("e.g. Wait for Summer Sale, bundle with DLC", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GamerBorder,
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val price = targetInput.toDoubleOrNull() ?: currentPrice
                            onSaveTarget(price, notesInput, priorityInput)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = GamerBg),
                        modifier = Modifier.testTag("save_target_price_button")
                    ) {
                        Text("Save Offline Alert", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PriceDropAlertSimulatorDialog(
    gameTitle: String,
    currentPrice: Double,
    currencySymbol: String,
    onSimulatePriceDrop: (discountPercent: Int) -> Unit,
    onResetPrice: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDiscount by remember { mutableIntStateOf(35) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = GamerSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("price_drop_simulator_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = NeonGreen.copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = "Price Drop Simulation",
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Simulate Price Drop",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GamerTextPrimary
                        )
                        Text(
                            text = "Mock live retailer price decrease",
                            fontSize = 11.sp,
                            color = GamerTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GamerSurfaceHighlight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = gameTitle,
                            fontWeight = FontWeight.Bold,
                            color = GamerTextPrimary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "Current Price: ",
                                fontSize = 12.sp,
                                color = GamerTextMuted
                            )
                            Text(
                                text = "$currencySymbol${"%.2f".format(currentPrice)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GamerTextPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "➔ New: ",
                                fontSize = 12.sp,
                                color = NeonGreen
                            )
                            val newCalculated = (currentPrice * (1.0 - selectedDiscount / 100.0)).coerceAtLeast(4.99)
                            Text(
                                text = "$currencySymbol${"%.2f".format(newCalculated)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = NeonGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Select Simulated Discount Scenario:",
                    fontSize = 12.sp,
                    color = GamerTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    20 to "⚡ 20% Off Weekend Sale",
                    35 to "🔥 35% Off Midweek Madness",
                    50 to "💥 50% Off Half-Price Deal",
                    70 to "🏆 70% Off All-Time Historical Low"
                ).forEach { (pct, label) ->
                    val isSelected = selectedDiscount == pct
                    Surface(
                        onClick = { selectedDiscount = pct },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) NeonGreen.copy(alpha = 0.15f) else GamerSurfaceHighlight,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonGreen) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .testTag("discount_option_$pct")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedDiscount = pct },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonGreen,
                                    unselectedColor = GamerTextMuted
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonGreen else GamerTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ℹ️ Simulating will push a real Android status bar notification and dynamically update the prices on this page.",
                    fontSize = 11.sp,
                    color = GamerTextMuted,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onResetPrice()
                            onDismiss()
                        }
                    ) {
                        Text("Reset Price", color = GamerTextMuted, fontSize = 12.sp)
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = GamerTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                onSimulatePriceDrop(selectedDiscount)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = GamerBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("confirm_simulate_price_drop_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Simulate",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Simulate Now",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountProfileDialog(
    user: com.google.firebase.auth.FirebaseUser?,
    wishlistCount: Int,
    libraryCount: Int,
    onSignOut: () -> Unit,
    onSignInPrompt: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GamerSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("account_profile_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Avatar
                Surface(
                    shape = CircleShape,
                    color = NeonCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (user != null) {
                            Text(
                                text = (user.displayName?.firstOrNull() ?: user.email?.firstOrNull() ?: 'U').uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                color = NeonCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = "Guest",
                                tint = GamerTextSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.displayName ?: "Guest Gamer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GamerTextPrimary
                )

                Text(
                    text = user?.email ?: "Browsing as guest (cloud sync inactive)",
                    fontSize = 12.sp,
                    color = if (user != null) NeonCyan else GamerTextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GamerSurfaceHighlight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$wishlistCount",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = NeonPink
                            )
                            Text("Wishlisted", fontSize = 11.sp, color = GamerTextSecondary)
                        }
                        VerticalDivider(modifier = Modifier.height(28.dp), color = GamerBorder)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$libraryCount",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = NeonGreen
                            )
                            Text("Library", fontSize = 11.sp, color = GamerTextSecondary)
                        }
                        VerticalDivider(modifier = Modifier.height(28.dp), color = GamerBorder)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (user != null) "Cloud" else "Local",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = if (user != null) NeonCyan else NeonAmber
                            )
                            Text("Sync", fontSize = 11.sp, color = GamerTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (user != null) {
                    Button(
                        onClick = {
                            onSignOut()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPink.copy(alpha = 0.2f),
                            contentColor = NeonPink
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("profile_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            onDismiss()
                            onSignInPrompt()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("profile_sign_in_button")
                    ) {
                        Text("Sign in with Google to Sync", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Close", color = GamerTextSecondary)
                }
            }
        }
    }
}

