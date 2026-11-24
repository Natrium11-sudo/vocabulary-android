package com.wortschatz.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = DuoGreen,
    onPrimary = LightDuoColors.greenSolidInk,
    primaryContainer = LightDuoColors.greenSoft,
    onPrimaryContainer = LightDuoColors.greenInk,
    secondary = DuoBlue,
    onSecondary = LightDuoColors.blueDeep,
    secondaryContainer = LightDuoColors.blueSoft,
    onSecondaryContainer = LightDuoColors.blueDeep,
    tertiary = DuoGold,
    onTertiary = LightDuoColors.goldSolidInk,
    error = DuoRed,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = LightDuoColors.redSoft,
    onErrorContainer = LightDuoColors.redDeep,
    background = LightDuoColors.background,
    onBackground = LightDuoColors.textPrimary,
    surface = LightDuoColors.surface,
    onSurface = LightDuoColors.textPrimary,
    surfaceVariant = LightDuoColors.surfaceMuted,
    onSurfaceVariant = LightDuoColors.textSecondary,
    outline = LightDuoColors.outline,
    outlineVariant = LightDuoColors.outline,
    scrim = LightDuoColors.scrim,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkDuoColors.green,
    onPrimary = DarkDuoColors.greenSolidInk,
    primaryContainer = DarkDuoColors.greenSoft,
    onPrimaryContainer = DarkDuoColors.greenInk,
    secondary = DarkDuoColors.blue,
    onSecondary = DarkDuoColors.blueDeep,
    secondaryContainer = DarkDuoColors.blueSoft,
    onSecondaryContainer = DarkDuoColors.blueDeep,
    tertiary = DarkDuoColors.gold,
    onTertiary = DarkDuoColors.goldSolidInk,
    error = DarkDuoColors.red,
    onError = androidx.compose.ui.graphics.Color(0xFF2B0000),
    errorContainer = DarkDuoColors.redSoft,
    onErrorContainer = DarkDuoColors.redDeep,
    background = DarkDuoColors.background,
    onBackground = DarkDuoColors.textPrimary,
    surface = DarkDuoColors.surface,
    onSurface = DarkDuoColors.textPrimary,
    surfaceVariant = DarkDuoColors.surfaceMuted,
    onSurfaceVariant = DarkDuoColors.textSecondary,
    outline = DarkDuoColors.outline,
    outlineVariant = DarkDuoColors.outline,
    scrim = DarkDuoColors.scrim,
)

val WortschatzShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Convenience accessor: `DuoTheme.colors.green`. */
object DuoTheme {
    val colors: DuoColors
        @Composable get() = LocalDuoColors.current
}

@Composable
fun WortschatzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val duoColors = if (darkTheme) DarkDuoColors else LightDuoColors

    CompositionLocalProvider(LocalDuoColors provides duoColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = WortschatzTypography,
            shapes = WortschatzShapes,
            content = content,
        )
    }
}
