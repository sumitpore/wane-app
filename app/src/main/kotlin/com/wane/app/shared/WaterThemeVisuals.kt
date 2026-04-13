package com.wane.app.shared

data class WaterThemeVisuals(
    val themeId: String,
    val wave1: WaveParams,
    val wave2: WaveParams,
    val wave3: WaveParams,
    val gradientTop: Long,
    val gradientUpper: Long,
    val gradientLower: Long,
    val gradientBottom: Long,
    val causticCenterColor: Long,
    val causticCount: Int,
    val causticBaseRadius: Float,
    val causticRadiusOscillation: Float,
    val backgroundStart: Long,
    val backgroundEnd: Long,
)

data class WaveParams(
    val color: Long,
    val amplitude: Float,
    val frequency: Float,
    val speed: Float,
)
