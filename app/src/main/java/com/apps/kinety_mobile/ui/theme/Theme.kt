package com.apps.kinety_mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode {
    SYSTEM,
    MERLOT_NOIR,
    PEARL_SILVER
}

private val MerlotNoirColorScheme = darkColorScheme(
    primary = MerlotPrimary,
    onPrimary = Color(0xFFFFF4F0),
    primaryContainer = MerlotAccentDark,
    onPrimaryContainer = Color(0xFFFFEAEA),
    secondary = MerlotCard,
    onSecondary = MerlotForeground,
    background = MerlotBackground,
    onBackground = MerlotForeground,
    surface = MerlotBackgroundRaised,
    onSurface = MerlotForeground,
    surfaceVariant = MerlotCard,
    onSurfaceVariant = MerlotMutedForeground,
    outline = MerlotBorder,
    outlineVariant = MerlotBorderSubtle
)

private val PearlSilverColorScheme = lightColorScheme(
    primary = PearlPrimary,
    onPrimary = Color.White,
    primaryContainer = PearlAccentDark,
    onPrimaryContainer = Color.White,
    secondary = PearlCard,
    onSecondary = PearlForeground,
    background = PearlBackground,
    onBackground = PearlForeground,
    surface = PearlBackgroundRaised,
    onSurface = PearlForeground,
    surfaceVariant = PearlCard,
    onSurfaceVariant = PearlMutedForeground,
    outline = PearlBorder,
    outlineVariant = PearlBorderSubtle
)

@Composable
fun KinetyTheme(
    themeMode: AppThemeMode = AppThemeMode.MERLOT_NOIR,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.MERLOT_NOIR -> true
        AppThemeMode.PEARL_SILVER -> false
    }

    val colorScheme = if (isDark) MerlotNoirColorScheme else PearlSilverColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}