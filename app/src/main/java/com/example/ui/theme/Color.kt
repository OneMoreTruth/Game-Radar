package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep Obsidian & Cyber Midnight Backgrounds (matches app icon background)
val GamerBg = Color(0xFF0A0E17)
val GamerSurface = Color(0xFF111625)
val GamerSurfaceElevated = Color(0xFF182035)
val GamerSurfaceHighlight = Color(0xFF222D4A)

// Vibrant Signature Neon Gradient Colors (sampled directly from the app icon)
val NeonCyan = Color(0xFF00F5D4)      // Left glow: Vibrant Mint / Neon Cyan
val NeonBlue = Color(0xFF00B4D8)      // Mid transition: Cyber Cyan-Blue
val NeonIndigo = Color(0xFF4361EE)    // Electric Indigo
val NeonPurple = Color(0xFF9D4EDD)    // Right glow: Electric Purple
val NeonViolet = Color(0xFFBD00FF)    // Vibrant Violet
val NeonPink = Color(0xFFFF2A85)      // Accent Pink
val NeonGreen = Color(0xFF10E7A8)     // Verified / Live Price Mint-Green
val NeonAmber = Color(0xFFFBBF24)     // Star Rating / Target Alert Amber

// Typography Colors
val GamerTextPrimary = Color(0xFFF8FAFC)
val GamerTextSecondary = Color(0xFF94A3B8)
val GamerTextMuted = Color(0xFF64748B)

// Borders & Badges
val GamerBorder = Color(0xFF253252)
val GamerDealBadge = Color(0xFF10E7A8)
val GamerMetaBadge = Color(0xFF4361EE)

// Signature Brand Gradients
val GameRadarBrandGradient = Brush.horizontalGradient(
    colors = listOf(NeonCyan, NeonBlue, NeonPurple)
)

val GameRadarAccentGradient = Brush.linearGradient(
    colors = listOf(NeonCyan, NeonPurple)
)
