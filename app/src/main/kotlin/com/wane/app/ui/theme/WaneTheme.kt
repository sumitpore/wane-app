package com.wane.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WaneDarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Crystalline,
    background = BackgroundDeep,
    onBackground = Crystalline,
    surface = BackgroundSettings,
    onSurface = Crystalline,
)

@Composable
fun WaneTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WaneDarkColorScheme,
        content = content,
    )
}
