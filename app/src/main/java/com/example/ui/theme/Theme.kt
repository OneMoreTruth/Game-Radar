package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkGamerColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = GamerBg,
    primaryContainer = GamerSurfaceHighlight,
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = GamerBg,
    secondaryContainer = GamerSurfaceElevated,
    onSecondaryContainer = NeonPurple,
    tertiary = NeonPink,
    onTertiary = GamerBg,
    background = GamerBg,
    onBackground = GamerTextPrimary,
    surface = GamerSurface,
    onSurface = GamerTextPrimary,
    surfaceVariant = GamerSurfaceElevated,
    onSurfaceVariant = GamerTextSecondary,
    outline = GamerBorder,
    error = NeonPink
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent bold gaming cyber aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkGamerColorScheme,
        typography = Typography,
        content = content
    )
}
