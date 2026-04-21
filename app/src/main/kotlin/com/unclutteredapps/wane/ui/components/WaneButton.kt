package com.unclutteredapps.wane.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unclutteredapps.wane.ui.theme.AccentPrimary
import com.unclutteredapps.wane.ui.theme.Crystalline
import com.unclutteredapps.wane.ui.theme.WaneMotion
import com.unclutteredapps.wane.ui.theme.WaneTheme
import com.unclutteredapps.wane.ui.theme.WaneTypography

@Composable
fun WaneButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AccentPrimary,
    contentColor: Color = Crystalline,
    disabledContainerColor: Color = containerColor.copy(alpha = 0.4f),
    disabledContentColor: Color = contentColor.copy(alpha = 0.4f),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) WaneMotion.PressScaleCta else 1f,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.85f),
        label = "button_scale",
    )

    val translationY by animateFloatAsState(
        targetValue = if (isPressed) WaneMotion.PressTranslateYDp else 0f,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.85f),
        label = "button_translate_y",
    )

    Button(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.translationY = translationY * density
                },
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = disabledContainerColor,
                disabledContentColor = disabledContentColor,
            ),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        interactionSource = interactionSource,
    ) {
        Text(
            text = text,
            style = WaneTypography.labelLarge,
        )
    }
}

@Preview
@Composable
private fun WaneButtonPreview() {
    WaneTheme {
        WaneButton(text = "Begin", onClick = {})
    }
}
