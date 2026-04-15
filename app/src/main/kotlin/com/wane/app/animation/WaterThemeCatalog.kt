package com.wane.app.animation

import com.wane.app.shared.WaterThemeVisuals
import com.wane.app.shared.WaveParams

/**
 * Compile-time [WaterThemeVisuals] for the water shader. UI resolves [themeId] here.
 */
object WaterThemeCatalog {
    private val stillWater =
        WaterThemeVisuals(
            themeId = "default",
            wave1 = WaveParams(color = 0x1A2878B4, amplitude = 0.009f, frequency = 1.1f, speed = 0.8f),
            wave2 = WaveParams(color = 0x141E5A96, amplitude = 0.006f, frequency = 1.6f, speed = 0.5f),
            wave3 = WaveParams(color = 0x0F144682, amplitude = 0.005f, frequency = 2.4f, speed = 1.0f),
            gradientTop = 0xFF1E5A8C,
            gradientUpper = 0xFF144173,
            gradientLower = 0xFF0C2A55,
            gradientBottom = 0xFF061632,
            causticCenterColor = 0x0D50A0DC,
            causticCount = 3,
            causticBaseRadius = 0.08f,
            causticRadiusOscillation = 0.03f,
            backgroundStart = 0xFF0A1628,
            backgroundEnd = 0xFF050C16,
        )

    val defaultVisuals: WaterThemeVisuals = stillWater

    fun getVisuals(themeId: String): WaterThemeVisuals? = ALL_THEMES[themeId]

    private val ALL_THEMES: Map<String, WaterThemeVisuals> =
        listOf(
            stillWater,
        ).associateBy { it.themeId }
}
