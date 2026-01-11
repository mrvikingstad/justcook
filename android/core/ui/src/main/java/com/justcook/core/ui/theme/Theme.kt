package com.justcook.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightText,
    onPrimary = LightSurface,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightText,
    secondary = LightTextMuted,
    onSecondary = LightSurface,
    secondaryContainer = LightBorder,
    onSecondaryContainer = LightText,
    tertiary = LightTextMuted,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightBackground,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorBgLight,
    onErrorContainer = ErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkText,
    onPrimary = DarkSurface,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkText,
    secondary = DarkTextMuted,
    onSecondary = DarkSurface,
    secondaryContainer = DarkBorder,
    onSecondaryContainer = DarkText,
    tertiary = DarkTextMuted,
    onTertiary = DarkSurface,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = ErrorDark,
    onError = Color.Black,
    errorContainer = ErrorBgDark,
    onErrorContainer = ErrorDark
)

/**
 * Custom colors not in Material spec
 */
data class JustCookColors(
    val textMuted: Color,
    val border: Color,
    val voteUpActive: Color,
    val voteDownActive: Color,
    val voteNeutral: Color,
    val success: Color
)

val LocalJustCookColors = staticCompositionLocalOf {
    JustCookColors(
        textMuted = LightTextMuted,
        border = LightBorder,
        voteUpActive = VoteUpActive,
        voteDownActive = VoteDownActive,
        voteNeutral = VoteNeutral,
        success = SuccessLight
    )
}

@Composable
fun JustCookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val justCookColors = JustCookColors(
        textMuted = if (darkTheme) DarkTextMuted else LightTextMuted,
        border = if (darkTheme) DarkBorder else LightBorder,
        voteUpActive = VoteUpActive,
        voteDownActive = VoteDownActive,
        voteNeutral = VoteNeutral,
        success = if (darkTheme) SuccessDark else SuccessLight
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalJustCookColors provides justCookColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = JustCookTypography,
            shapes = JustCookShapes,
            content = content
        )
    }
}
