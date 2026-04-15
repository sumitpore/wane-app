package com.wane.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.DotInactive
import com.wane.app.ui.theme.WaneTheme

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage

            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                animationSpec = spring(stiffness = 100f, dampingRatio = 0.85f),
                label = "dot_width_$index",
            )

            Box(
                modifier =
                    Modifier
                        .size(width = width, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) AccentPrimary else DotInactive),
            )
        }
    }
}

@Preview
@Composable
private fun PageIndicatorPreview() {
    WaneTheme {
        PageIndicator(pageCount = 3, currentPage = 1)
    }
}
