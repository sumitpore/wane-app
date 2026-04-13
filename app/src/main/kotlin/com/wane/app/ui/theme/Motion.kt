package com.wane.app.ui.theme

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp

object WaneMotion {

    val SpringDefault: SpringSpec<Float> = spring(
        dampingRatio = 0.7f,
        stiffness = 100f,
    )

    val ScreenFadeIn: TweenSpec<Float> = tween(
        durationMillis = 600,
        easing = EaseOut,
    )

    val SessionEntry: TweenSpec<Float> = tween(
        durationMillis = 800,
        easing = EaseOut,
    )

    val StaggerIntervalHome = 200L
    val StaggerIntervalSettings = 50L

    val DotTransition: TweenSpec<Dp> = tween(durationMillis = 300)

    const val PressScaleCircle = 0.95f
    const val PressScaleCta = 0.97f
    const val PressTranslateYDp = -1f
}
