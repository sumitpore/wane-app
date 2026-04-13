package com.wane.app.animation

import com.wane.app.shared.WaterThemeVisuals
import com.wane.app.shared.WaveParams

/**
 * Compile-time [WaterThemeVisuals] for the water shader. UI resolves [themeId] here.
 */
object WaterThemeCatalog {

    private val stillWater = WaterThemeVisuals(
        themeId = "default",
        wave1 = WaveParams(color = 0x6638A3DC, amplitude = 0.03f, frequency = 1.5f, speed = 0.8f),
        wave2 = WaveParams(color = 0x4464B8E8, amplitude = 0.02f, frequency = 2.0f, speed = 1.2f),
        wave3 = WaveParams(color = 0x3338A3DC, amplitude = 0.015f, frequency = 2.5f, speed = 0.6f),
        gradientTop = 0xFF0A1628,
        gradientUpper = 0xFF0D1F3C,
        gradientLower = 0xFF102A50,
        gradientBottom = 0xFF081420,
        causticCenterColor = 0x3364B8E8,
        causticCount = 5,
        causticBaseRadius = 0.08f,
        causticRadiusOscillation = 0.03f,
        backgroundStart = 0xFF060E1A,
        backgroundEnd = 0xFF0A1628,
    )

    private val monsoon = WaterThemeVisuals(
        themeId = "monsoon",
        wave1 = WaveParams(0x882E8CBC, 0.045f, 1.8f, 1.1f),
        wave2 = WaveParams(0x55206088, 0.035f, 2.4f, 1.5f),
        wave3 = WaveParams(0x44184870, 0.028f, 3.0f, 0.9f),
        gradientTop = 0xFF061018,
        gradientUpper = 0xFF0A1A30,
        gradientLower = 0xFF081428,
        gradientBottom = 0xFF040810,
        causticCenterColor = 0x554090C8,
        causticCount = 6,
        causticBaseRadius = 0.09f,
        causticRadiusOscillation = 0.04f,
        backgroundStart = 0xFF040810,
        backgroundEnd = 0xFF081828,
    )

    private val glacier = WaterThemeVisuals(
        themeId = "glacier",
        wave1 = WaveParams(0x5590C8E8, 0.018f, 1.2f, 0.45f),
        wave2 = WaveParams(0x4478B0D8, 0.014f, 1.6f, 0.55f),
        wave3 = WaveParams(0x3368A0C8, 0.012f, 2.0f, 0.4f),
        gradientTop = 0xFF1A3048,
        gradientUpper = 0xFF142838,
        gradientLower = 0xFF0E2030,
        gradientBottom = 0xFF081820,
        causticCenterColor = 0x44A8D8F0,
        causticCount = 4,
        causticBaseRadius = 0.1f,
        causticRadiusOscillation = 0.025f,
        backgroundStart = 0xFF0C1828,
        backgroundEnd = 0xFF142838,
    )

    private val koi = WaterThemeVisuals(
        themeId = "koi",
        wave1 = WaveParams(0x66D4A020, 0.032f, 1.6f, 0.85f),
        wave2 = WaveParams(0x44B87818, 0.024f, 2.1f, 1.0f),
        wave3 = WaveParams(0x33906010, 0.018f, 2.6f, 0.65f),
        gradientTop = 0xFF281808,
        gradientUpper = 0xFF201408,
        gradientLower = 0xFF180C04,
        gradientBottom = 0xFF100804,
        causticCenterColor = 0x55F0C860,
        causticCount = 5,
        causticBaseRadius = 0.085f,
        causticRadiusOscillation = 0.035f,
        backgroundStart = 0xFF140C04,
        backgroundEnd = 0xFF201408,
    )

    private val bioluminescence = WaterThemeVisuals(
        themeId = "bioluminescence",
        wave1 = WaveParams(0x6630E8C8, 0.035f, 1.7f, 0.95f),
        wave2 = WaveParams(0x4420C8A0, 0.026f, 2.2f, 1.25f),
        wave3 = WaveParams(0x3318A878, 0.02f, 2.8f, 0.7f),
        gradientTop = 0xFF082828,
        gradientUpper = 0xFF061F20,
        gradientLower = 0xFF041818,
        gradientBottom = 0xFF021010,
        causticCenterColor = 0x6648F8B8,
        causticCount = 7,
        causticBaseRadius = 0.075f,
        causticRadiusOscillation = 0.045f,
        backgroundStart = 0xFF041010,
        backgroundEnd = 0xFF081C18,
    )

    val defaultVisuals: WaterThemeVisuals = stillWater

    fun getVisuals(themeId: String): WaterThemeVisuals? = ALL_THEMES[themeId]

    fun getAllVisuals(): List<WaterThemeVisuals> = ALL_THEMES.values.toList()

    private val ALL_THEMES: Map<String, WaterThemeVisuals> = listOf(
        stillWater,
        monsoon,
        glacier,
        koi,
        bioluminescence,
    ).associateBy { it.themeId }
}
