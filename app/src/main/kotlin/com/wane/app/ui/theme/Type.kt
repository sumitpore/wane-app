package com.wane.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Font families will be initialized with actual font resources in Phase 4.
// For now, use default sans-serif as placeholder until TTF files are bundled.
val Sora = FontFamily.Default
val DmSans = FontFamily.Default
val SpaceGrotesk = FontFamily.Default

object WaneTypography {

    val displayLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 64.sp,
        letterSpacing = (-0.03).sp,
    )

    val headlineLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        letterSpacing = (-0.03).sp,
    )

    val headlineMedium = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = (-0.02).sp,
    )

    val bodyLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.6.sp,
    )

    val bodyMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.4.sp,
    )

    val labelLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    )

    val labelMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
    )

    val labelSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.8.sp,
    )

    val labelMicro = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp,
    )
}
