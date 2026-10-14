package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightFistShotScheme = lightColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = AppleBlueLight,
    onPrimaryContainer = AppleBlue,
    secondary = AppleTeal,
    onSecondary = Color.White,
    secondaryContainer = AppleTealLight,
    onSecondaryContainer = AppleTeal,
    tertiary = AppleOrange,
    onTertiary = Color.White,
    tertiaryContainer = AppleOrangeLight,
    onTertiaryContainer = AppleOrange,
    background = QuartzBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = FrostedSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle,
    error = AppleRed,
    onError = Color.White
)

@Composable
fun FistShotTheme(
    darkTheme: Boolean = false, // Default to light theme minimalism as specifically instructed
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightFistShotScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FistShotTheme(darkTheme = false, content = content)
}
