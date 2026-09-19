package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = SpaceDarkBg,
    primaryContainer = SpaceSurfaceVariant,
    onPrimaryContainer = CyberCyan,
    secondary = NeonPurple,
    onSecondary = SpaceDarkBg,
    secondaryContainer = SpaceSurface,
    onSecondaryContainer = NeonPurple,
    tertiary = NeonEmerald,
    onTertiary = SpaceDarkBg,
    background = SpaceDarkBg,
    onBackground = TextPrimary,
    surface = SpaceSurface,
    onSurface = TextPrimary,
    surfaceVariant = SpaceCardBg,
    onSurfaceVariant = TextSecondary,
    outline = SpaceCardBorder,
    error = AlertRed,
    onError = SpaceDarkBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

