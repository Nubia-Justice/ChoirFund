package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = ForestGreenPrimary,
    secondary = ForestGreenLight,
    onSecondary = Color.White,
    tertiary = ForestGreenPrimary,
    background = WarmOffWhiteBackground,
    onBackground = NearBlackGreenText,
    surface = PureWhiteCard,
    onSurface = NearBlackGreenText,
    surfaceVariant = WarmOffWhiteBackground,
    onSurfaceVariant = MutedGreenTextSecondary,
    outline = SoftBeigeBorder,
    outlineVariant = SoftBeigeBorder,
    error = MutedRedWarning,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftLightGreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1E3824),
    onPrimaryContainer = SoftLightGreenPrimary,
    secondary = SoftLightGreenPrimary,
    onSecondary = Color.Black,
    background = DarkCharcoalGreenBg,
    onBackground = DarkOffWhiteText,
    surface = DarkCardBg,
    onSurface = DarkOffWhiteText,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = DarkMutedTextSecondary,
    outline = DarkGreenBorder,
    outlineVariant = DarkGreenBorder,
    error = DarkWarningRed,
    onError = Color.White
)

// Shape: corners are rounded at 8px (PRD section 6)
val ChoirShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp), // 8px default
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun MyApplicationTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ChoirShapes,
        content = content
    )
}
