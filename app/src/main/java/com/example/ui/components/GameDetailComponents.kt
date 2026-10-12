package com.example.ui.components

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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SpecDetails
import com.example.data.model.SystemRequirements
import com.example.data.model.UserReview
import com.example.ui.theme.*

@Composable
fun SystemRequirementsSection(
    requirements: SystemRequirements?,
    modifier: Modifier = Modifier
) {
    if (requirements == null) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GamerSurfaceElevated,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Console",
                    tint = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Console Native Title",
                    fontWeight = FontWeight.Bold,
                    color = GamerTextPrimary
                )
                Text(
                    text = "Optimized directly for console hardware (No PC specs required).",
                    fontSize = 12.sp,
                    color = GamerTextSecondary
                )
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamerSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("system_requirements_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "Hardware",
                    tint = NeonCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PC System Requirements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GamerTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab switch between Minimum and Recommended
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GamerSurfaceHighlight)
                    .padding(4.dp)
            ) {
                val minActive = selectedTab == 0
                Surface(
                    onClick = { selectedTab = 0 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (minActive) NeonCyan else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Minimum Specs",
                            fontWeight = if (minActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (minActive) GamerBg else GamerTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                val recActive = selectedTab == 1
                Surface(
                    onClick = { selectedTab = 1 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (recActive) NeonPurple else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Recommended Specs (60+ FPS)",
                            fontWeight = if (recActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (recActive) GamerBg else GamerTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val currentSpecs = if (selectedTab == 0) requirements.minimum else requirements.recommended

            SpecRowItem(label = "Operating System", value = currentSpecs.os, icon = Icons.Default.Laptop)
            SpecRowItem(label = "Processor (CPU)", value = currentSpecs.processor, icon = Icons.Default.Memory)
            SpecRowItem(label = "Memory (RAM)", value = currentSpecs.memory, icon = Icons.Default.Storage)
            SpecRowItem(label = "Graphics (GPU)", value = currentSpecs.graphics, icon = Icons.Default.VideogameAsset)
            SpecRowItem(label = "Storage Space", value = currentSpecs.storage, icon = Icons.Default.Save)
            SpecRowItem(label = "DirectX", value = currentSpecs.directX, icon = Icons.Default.Settings)
        }
    }
}

@Composable
private fun SpecRowItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NeonCyan.copy(alpha = 0.8f),
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = GamerTextMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = GamerTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TrailerPlayerPreview(
    youtubeVideoId: String,
    title: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val thumbnailUrl = "https://img.youtube.com/vi/$youtubeVideoId/maxresdefault.jpg"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamerSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=$youtubeVideoId")
                    )
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // ignore
                }
            }
            .testTag("trailer_player_preview")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Trailer Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, GamerBg.copy(alpha = 0.85f))
                            )
                        )
                )

                // Big play button
                Surface(
                    shape = CircleShape,
                    color = NeonPink,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // HD 4K badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "4K 60FPS",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                    Text(
                        text = "Tap to stream official cinematic gameplay trailer",
                        fontSize = 12.sp,
                        color = GamerTextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open Trailer",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun UserReviewItemCard(
    review: UserReview,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GamerSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GamerBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_card_${review.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gamer avatar badge
                Surface(
                    shape = CircleShape,
                    color = NeonPurple.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = review.reviewerName.take(1).uppercase(),
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.reviewerName,
                            fontWeight = FontWeight.Bold,
                            color = GamerTextPrimary,
                            fontSize = 14.sp
                        )
                        if (review.isVerifiedGamer) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Gamer",
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.platform,
                            fontSize = 11.sp,
                            color = GamerTextSecondary
                        )
                        Text(text = " • ", fontSize = 11.sp, color = GamerTextMuted)
                        Text(
                            text = "${review.hoursPlayed} hrs logged",
                            fontSize = 11.sp,
                            color = NeonGreen
                        )
                    }
                }

                // Rating badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonAmber.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = NeonAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${review.rating}",
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = review.reviewText,
                fontSize = 13.sp,
                color = GamerTextPrimary,
                lineHeight = 18.sp
            )

            // Pros & Cons pills
            if (review.pros.isNotEmpty() || review.cons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    review.pros.take(2).forEach { pro ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonGreen.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "✓ $pro",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    review.cons.take(1).forEach { con ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonPink.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "✗ $con",
                                color = NeonPink,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
