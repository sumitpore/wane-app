package com.wane.app.ui.home

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wane.app.R
import com.wane.app.service.SessionError
import com.wane.app.ui.theme.AccentLight
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.BackgroundDeep
import com.wane.app.ui.theme.BackgroundDeepEnd
import com.wane.app.ui.theme.BackgroundDeepMid
import com.wane.app.ui.theme.Crystalline
import com.wane.app.ui.theme.Sora
import com.wane.app.ui.theme.TextMuted
import com.wane.app.ui.theme.TextPrimary
import com.wane.app.ui.theme.TextSecondary
import com.wane.app.ui.theme.TextStatus
import com.wane.app.ui.theme.WaneMotion
import com.wane.app.ui.theme.WaneTheme
import com.wane.app.ui.theme.WaneTypography

@Composable
fun HomeScreen(
    onNavigateToSession: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToSession -> onNavigateToSession()
                is HomeEffect.ShowError -> {
                    val message = when (effect.error) {
                        is SessionError.ForegroundServiceBlocked ->
                            context.getString(R.string.error_foreground_service)
                        is SessionError.AccessibilityServiceDisabled ->
                            context.getString(R.string.error_accessibility_service)
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onStartSession = {
            val durationMs = uiState.defaultDuration * 60L * 1000L
            viewModel.onEvent(HomeUiEvent.StartSession(durationMs))
        },
        onSettings = onNavigateToSettings,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onStartSession: () -> Unit,
    onSettings: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDeep, BackgroundDeepEnd),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        tint = TextMuted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.welcome_title),
                style = WaneTypography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraLight,
                ),
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(0.15f))

            StreakDisplay(streak = uiState.currentStreak)

            Spacer(modifier = Modifier.weight(0.2f))

            StartButton(onClick = onStartSession)

            Spacer(modifier = Modifier.height(32.dp))

            DurationLabel(duration = uiState.defaultDuration)

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

@Composable
private fun StreakDisplay(streak: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (streak > 0) {
            Text(
                text = streak.toString(),
                fontFamily = Sora,
                fontWeight = FontWeight.ExtraLight,
                fontSize = 56.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.day_streak),
                style = WaneTypography.labelMedium,
                color = TextStatus,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = stringResource(R.string.no_streak_yet),
                style = WaneTypography.bodyLarge,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StartButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) WaneMotion.PressScaleCircle else 1f,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.85f),
        label = "start_scale",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "start_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_pulse",
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.size(180.dp),
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f * glowPulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentPrimary.copy(alpha = 0.2f),
                        AccentLight.copy(alpha = 0.08f),
                        BackgroundDeepMid.copy(alpha = 0f),
                    ),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }

        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            shape = CircleShape,
            containerColor = AccentPrimary,
            contentColor = Crystalline,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
            ),
            interactionSource = interactionSource,
        ) {
            Text(
                text = stringResource(R.string.start_session),
                style = WaneTypography.labelLarge,
                color = Crystalline,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DurationLabel(duration: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.minutes_format, duration),
            style = WaneTypography.bodyLarge,
            color = TextStatus,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun HomeContentPreview() {
    WaneTheme {
        HomeContent(
            uiState = HomeUiState(currentStreak = 5, defaultDuration = 25),
            onStartSession = {},
            onSettings = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun HomeContentNoStreakPreview() {
    WaneTheme {
        HomeContent(
            uiState = HomeUiState(currentStreak = 0, defaultDuration = 25),
            onStartSession = {},
            onSettings = {},
        )
    }
}
