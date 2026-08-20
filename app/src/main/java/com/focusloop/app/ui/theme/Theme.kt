package com.focusloop.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = FocusPurpleLight,
    onPrimary = Color.White,
    primaryContainer = FocusPurpleDark,
    onPrimaryContainer = FocusPurpleLight,
    secondary = FocusTeal,
    onSecondary = Color.Black,
    secondaryContainer = FocusTealDark,
    onSecondaryContainer = FocusTealLight,
    tertiary = FocusOrange,
    onTertiary = Color.White,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF3A3860)
)

private val LightColorScheme = lightColorScheme(
    primary = FocusPurple,
    onPrimary = Color.White,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = FocusPurpleDark,
    secondary = FocusTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0FFF8),
    onSecondaryContainer = FocusTealDark,
    tertiary = FocusOrange,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFDDDBEE)
)

@Composable
fun FocusLoopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FocusTypography,
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8),
            small = androidx.compose.foundation.shape.RoundedCornerShape(12),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(16),
            large = androidx.compose.foundation.shape.RoundedCornerShape(24),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32)
        ),
        content = content
    )
}
