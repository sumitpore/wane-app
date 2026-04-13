package com.wane.app.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wane.app.R
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.BodyText
import com.wane.app.ui.theme.DotInactive
import com.wane.app.ui.theme.SurfaceGlass
import com.wane.app.ui.theme.TextSecondary
import com.wane.app.ui.theme.WaneTheme
import com.wane.app.ui.theme.WaneTypography

@Composable
fun AutoLockStep(
    autoLockEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.auto_lock_title),
            style = WaneTypography.headlineLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.auto_lock_description),
            style = WaneTypography.bodyLarge,
            color = BodyText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp),
        )

        Spacer(modifier = Modifier.height(48.dp))

        AutoLockToggleRow(
            enabled = autoLockEnabled,
            onToggle = onToggle,
        )
    }
}

@Composable
private fun AutoLockToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGlass)
            .clickable(role = Role.Switch) { onToggle(!enabled) }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.auto_lock_toggle_label),
            style = WaneTypography.labelLarge,
            color = TextSecondary,
        )

        WaneToggle(checked = enabled)
    }
}

@Composable
private fun WaneToggle(checked: Boolean) {
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.85f),
        label = "toggle_thumb",
    )

    val trackColor = if (checked) AccentPrimary else DotInactive

    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(22.dp)
                .graphicsLayer { translationX = thumbOffset * (48.dp.toPx() - 28.dp.toPx()) }
                .clip(RoundedCornerShape(11.dp))
                .background(TextSecondary),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun AutoLockStepPreview() {
    WaneTheme {
        AutoLockStep(autoLockEnabled = false, onToggle = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun AutoLockStepEnabledPreview() {
    WaneTheme {
        AutoLockStep(autoLockEnabled = true, onToggle = {})
    }
}
