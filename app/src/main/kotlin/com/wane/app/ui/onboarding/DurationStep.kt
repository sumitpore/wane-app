package com.wane.app.ui.onboarding

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wane.app.R
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.Sora
import com.wane.app.ui.theme.SurfaceGlass
import com.wane.app.ui.theme.TextMuted
import com.wane.app.ui.theme.TextPrimary
import com.wane.app.ui.theme.TextSecondary
import com.wane.app.ui.theme.WaneTheme
import com.wane.app.ui.theme.WaneTypography

private const val MIN_DURATION = 5
private const val MAX_DURATION = 120
private const val DURATION_STEP = 5

@Composable
fun DurationStep(
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedDuration by animateIntAsState(
        targetValue = selectedDuration,
        animationSpec = tween(durationMillis = 300),
        label = "duration_anim",
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.duration_title),
            style = WaneTypography.headlineLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = animatedDuration.toString(),
            fontFamily = Sora,
            fontWeight = FontWeight.ExtraLight,
            fontSize = 96.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.minutes_label),
            style = WaneTypography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundControlButton(
                symbol = "\u2212",
                enabled = selectedDuration > MIN_DURATION,
                onClick = {
                    onDurationChange((selectedDuration - DURATION_STEP).coerceAtLeast(MIN_DURATION))
                },
            )

            RoundControlButton(
                symbol = "+",
                enabled = selectedDuration < MAX_DURATION,
                onClick = {
                    onDurationChange((selectedDuration + DURATION_STEP).coerceAtMost(MAX_DURATION))
                },
            )
        }
    }
}

@Composable
private fun RoundControlButton(
    symbol: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = SurfaceGlass,
        modifier =
            Modifier
                .size(56.dp)
                .alpha(if (enabled) 1f else 0.4f)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = AccentPrimary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun DurationStepPreview() {
    WaneTheme {
        DurationStep(selectedDuration = 25, onDurationChange = {})
    }
}
