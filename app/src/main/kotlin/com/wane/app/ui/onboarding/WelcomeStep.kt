package com.wane.app.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wane.app.R
import com.wane.app.ui.theme.AccentLight
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.BackgroundDeepMid
import com.wane.app.ui.theme.TextSecondary
import com.wane.app.ui.theme.WaneTheme
import com.wane.app.ui.theme.WaneTypography

@Composable
fun WelcomeStep(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_glow")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_radius",
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp),
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawGlowOrb(glowRadius)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.welcome_title),
            style = WaneTypography.displayLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.welcome_subtitle),
            style = WaneTypography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private fun DrawScope.drawGlowOrb(pulse: Float) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = size.minDimension / 2f * pulse
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                AccentPrimary.copy(alpha = 0.35f),
                AccentLight.copy(alpha = 0.15f),
                BackgroundDeepMid.copy(alpha = 0f),
            ),
            center = center,
            radius = maxRadius,
        ),
        radius = maxRadius,
        center = center,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun WelcomeStepPreview() {
    WaneTheme {
        WelcomeStep()
    }
}
