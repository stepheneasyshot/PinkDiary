package com.stephen.pinkdiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = OnPinkPrimary,
    primaryContainer = PinkPrimaryContainer,
    onPrimaryContainer = OnPinkPrimaryContainer,
    secondary = PinkSecondary,
    onSecondary = OnPinkSecondary,
    secondaryContainer = PinkSecondaryContainer,
    onSecondaryContainer = OnPinkSecondaryContainer,
    tertiary = PinkSecondary,
    onTertiary = OnPinkSecondary,
    background = WhiteBackground,
    onBackground = WarmInk,
    surface = WhiteBackground,
    onSurface = WarmInk,
    surfaceVariant = PinkPrimaryContainer,
    onSurfaceVariant = MauveInk
)

private val DarkColors = darkColorScheme(
    primary = PredictedPinkDark,
    onPrimary = Color(0xFF5C1130),
    primaryContainer = Color(0xFF7D5260),
    onPrimaryContainer = PinkPrimaryContainer,
    secondary = PredictedPinkDark,
    onSecondary = Color(0xFF5C1130),
    background = Color(0xFF1C1316),
    onBackground = Color(0xFFF3E7EA),
    surface = Color(0xFF1C1316),
    onSurface = Color(0xFFF3E7EA),
    surfaceContainerLow = Color(0xFF3A2930),
    surfaceVariant = Color(0xFF2D1B23),
    onSurfaceVariant = Color(0xFFD8C0C8)
)

@Composable
fun PinkdiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
