package com.wane.app.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.wane.app.shared.TiltState
import com.wane.app.shared.WaterThemeVisuals

/**
 * Hosts [WaterSurfaceView] and forwards session state into the GL renderer. When the shader fails to
 * compile, shows a static vertical gradient that approximates the theme background.
 */
@Composable
fun WaterCanvas(
    waterLevel: Float,
    tiltState: TiltState,
    themeVisuals: WaterThemeVisuals,
    modifier: Modifier = Modifier,
) {
    val level = waterLevel.coerceIn(0f, 1f)
    var shaderFailed by remember { mutableStateOf(false) }

    if (shaderFailed) {
        StaticWaterFallback(themeVisuals = themeVisuals, modifier = modifier.fillMaxSize())
        return
    }

    AndroidView(
        factory = { ctx ->
            WaterSurfaceView(ctx).apply {
                onShaderFailedChanged = { failed ->
                    if (failed) shaderFailed = true
                }
                updateWaterLevel(level)
                updateTiltState(tiltState)
                updateThemeVisuals(themeVisuals)
            }
        },
        update = { view ->
            view.updateWaterLevel(level)
            view.updateTiltState(tiltState)
            view.updateThemeVisuals(themeVisuals)
            if (view.isShaderFailed()) {
                shaderFailed = true
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun StaticWaterFallback(
    themeVisuals: WaterThemeVisuals,
    modifier: Modifier = Modifier,
) {
    val top = Color(themeVisuals.backgroundStart)
    val bottom = Color(themeVisuals.backgroundEnd)
    Canvas(modifier = modifier) {
        drawRect(brush = Brush.verticalGradient(listOf(top, bottom)))
    }
}
