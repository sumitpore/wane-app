package com.wane.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class WaneColors(
    val backgroundDeep: Color = BackgroundDeep,
    val backgroundDeepMid: Color = BackgroundDeepMid,
    val backgroundDeepEnd: Color = BackgroundDeepEnd,
    val backgroundAbyss: Color = BackgroundAbyss,
    val backgroundSettings: Color = BackgroundSettings,
    val backgroundOverlay: Color = BackgroundOverlay,
    val accentPrimary: Color = AccentPrimary,
    val accentLight: Color = AccentLight,
    val crystalline: Color = Crystalline,
    val mutedTide: Color = MutedTide,
    val bodyText: Color = BodyText,
    val dotInactive: Color = DotInactive,
    val divider: Color = Divider,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textTertiary: Color = TextTertiary,
    val textStatus: Color = TextStatus,
    val textMuted: Color = TextMuted,
    val textSubtle: Color = TextSubtle,
    val textGhost: Color = TextGhost,
    val textGhostActive: Color = TextGhostActive,
    val surfaceGlass: Color = SurfaceGlass,
    val surfaceDim: Color = SurfaceDim,
    val borderSubtle: Color = BorderSubtle,
)

private val LocalWaneColors = staticCompositionLocalOf { WaneColors() }
private val LocalWaneTypography = staticCompositionLocalOf { WaneTypography }

private val WaneDarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Crystalline,
    background = BackgroundDeep,
    onBackground = Crystalline,
    surface = BackgroundSettings,
    onSurface = Crystalline,
)

object WaneTheme {
    val colors: WaneColors
        @Composable
        get() = LocalWaneColors.current

    val typography: WaneTypography
        @Composable
        get() = LocalWaneTypography.current
}

@Composable
fun WaneTheme(
    content: @Composable () -> Unit,
) {
    val waneColors = WaneColors()

    CompositionLocalProvider(
        LocalWaneColors provides waneColors,
        LocalWaneTypography provides WaneTypography,
    ) {
        MaterialTheme(
            colorScheme = WaneDarkColorScheme,
            content = content,
        )
    }
}
