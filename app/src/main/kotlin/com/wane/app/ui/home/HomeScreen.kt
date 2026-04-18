package com.wane.app.ui.home

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wane.app.R
import com.wane.app.service.SessionError
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.BackgroundDeep
import com.wane.app.ui.theme.BackgroundDeepEnd
import com.wane.app.ui.theme.BackgroundDeepMid
import com.wane.app.ui.theme.Sora
import com.wane.app.ui.theme.SpaceGrotesk
import com.wane.app.ui.theme.SurfaceGlass
import com.wane.app.ui.theme.TextMuted
import com.wane.app.ui.theme.TextPrimary
import com.wane.app.ui.theme.TextSubtle
import com.wane.app.ui.theme.WaneMotion
import com.wane.app.ui.theme.WaneTheme
import com.wane.app.ui.theme.WaneTypography

private val DURATION_OPTIONS = HomeViewModel.DURATION_OPTIONS

@Composable
fun HomeScreen(
    onNavigateToSession: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(HomeUiEvent.RefreshPermissions)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToSession -> {
                    onNavigateToSession()
                }

                is HomeEffect.ShowError -> {
                    when (effect.error) {
                        is SessionError.ForegroundServiceBlocked -> {
                            val message = context.getString(R.string.error_foreground_service)
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
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
        onIncreaseDuration = { viewModel.onEvent(HomeUiEvent.IncreaseDuration) },
        onDecreaseDuration = { viewModel.onEvent(HomeUiEvent.DecreaseDuration) },
        onSettings = onNavigateToSettings,
        onOpenAccessibilitySettings = { viewModel.onEvent(HomeUiEvent.OpenAccessibilitySettings) },
        onOpenNotificationSettings = { viewModel.onEvent(HomeUiEvent.OpenNotificationSettings) },
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onStartSession: () -> Unit,
    onIncreaseDuration: () -> Unit,
    onDecreaseDuration: () -> Unit,
    onSettings: () -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
) {
    val currentIndex =
        DURATION_OPTIONS
            .indexOfFirst { it >= uiState.defaultDuration }
            .takeIf { it >= 0 } ?: 0
    val canIncrease = currentIndex < DURATION_OPTIONS.lastIndex
    val canDecrease = currentIndex > 0

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BackgroundDeep, BackgroundDeepMid, BackgroundDeepEnd),
                    ),
                ).statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        // Ambient glow behind center area
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glowCenter = Offset(size.width / 2f, size.height * 0.52f)
            val glowRadius = 150.dp.toPx()
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                AccentPrimary.copy(alpha = 0.08f),
                                Color.Transparent,
                            ),
                        center = glowCenter,
                        radius = glowRadius,
                    ),
                radius = glowRadius,
                center = glowCenter,
            )
        }

        // Settings gear — top right
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(end = 8.dp, top = 4.dp),
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

        // Main vertical layout: logo -> duration picker -> start button -> micro label
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            // Logo
            WaneLogo()

            Spacer(modifier = Modifier.weight(0.18f))

            // Duration picker
            DurationPicker(
                duration = uiState.defaultDuration,
                canIncrease = canIncrease,
                canDecrease = canDecrease,
                onIncrease = onIncreaseDuration,
                onDecrease = onDecreaseDuration,
            )

            Spacer(modifier = Modifier.weight(0.18f))

            // Start button
            StartButton(onClick = onStartSession)

            Spacer(modifier = Modifier.height(16.dp))

            // Micro-label
            Text(
                text = stringResource(R.string.begin_focus_session).uppercase(),
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = TextSubtle,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
            )

            if (uiState.accessibilityDisabled) {
                Spacer(modifier = Modifier.height(24.dp))
                AccessibilityPromptBanner(
                    onEnableClick = onOpenAccessibilitySettings,
                )
            }

            if (uiState.notificationListenerDisabled) {
                Spacer(modifier = Modifier.height(if (uiState.accessibilityDisabled) 12.dp else 24.dp))
                NotificationPromptBanner(
                    onEnableClick = onOpenNotificationSettings,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.privacy_label).uppercase(),
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = TextMuted,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSettings,
                    ),
            )

            Spacer(modifier = Modifier.weight(0.22f))
        }
    }
}

@Composable
private fun WaneLogo() {
    Image(
        painter = painterResource(R.drawable.wane_logo),
        contentDescription = stringResource(R.string.wane_logo),
        modifier = Modifier.height(48.dp),
        contentScale = ContentScale.FillHeight,
    )
}

@Composable
private fun AccessibilityPromptBanner(onEnableClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val fullText = stringResource(R.string.accessibility_prompt_message)
    val linkDisplay = "github.com/sumitpore/wane-app"
    val linkUrl = stringResource(R.string.open_source_url)
    val linkStart = fullText.indexOf(linkDisplay)
    val annotatedMessage =
        buildAnnotatedString {
            append(fullText)
            addStyle(SpanStyle(color = TextPrimary), 0, fullText.length)
            if (linkStart >= 0) {
                addStyle(
                    SpanStyle(color = AccentPrimary, textDecoration = TextDecoration.Underline),
                    linkStart,
                    linkStart + linkDisplay.length,
                )
                addStringAnnotation("URL", linkUrl, linkStart, linkStart + linkDisplay.length)
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = SurfaceGlass,
                    shape = RoundedCornerShape(16.dp),
                ).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClickableText(
            text = annotatedMessage,
            style = WaneTypography.bodyMedium.copy(textAlign = TextAlign.Center),
            onClick = { offset ->
                annotatedMessage
                    .getStringAnnotations("URL", offset, offset)
                    .firstOrNull()
                    ?.let { uriHandler.openUri(it.item) }
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onEnableClick,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = stringResource(R.string.enable_accessibility),
                style = WaneTypography.labelLarge,
            )
        }
    }
}

@Composable
private fun NotificationPromptBanner(onEnableClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val fullText = stringResource(R.string.notification_prompt_message)
    val linkDisplay = "github.com/sumitpore/wane-app"
    val linkUrl = stringResource(R.string.open_source_url)
    val linkStart = fullText.indexOf(linkDisplay)
    val annotatedMessage =
        buildAnnotatedString {
            append(fullText)
            addStyle(SpanStyle(color = TextPrimary), 0, fullText.length)
            if (linkStart >= 0) {
                addStyle(
                    SpanStyle(color = AccentPrimary, textDecoration = TextDecoration.Underline),
                    linkStart,
                    linkStart + linkDisplay.length,
                )
                addStringAnnotation("URL", linkUrl, linkStart, linkStart + linkDisplay.length)
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = SurfaceGlass,
                    shape = RoundedCornerShape(16.dp),
                ).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClickableText(
            text = annotatedMessage,
            style = WaneTypography.bodyMedium.copy(textAlign = TextAlign.Center),
            onClick = { offset ->
                annotatedMessage
                    .getStringAnnotations("URL", offset, offset)
                    .firstOrNull()
                    ?.let { uriHandler.openUri(it.item) }
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onEnableClick,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = stringResource(R.string.enable_notification_access),
                style = WaneTypography.labelLarge,
            )
        }
    }
}

@Composable
private fun DurationPicker(
    duration: Int,
    canIncrease: Boolean,
    canDecrease: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Chevron up
        IconButton(
            onClick = onIncrease,
            enabled = canIncrease,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.increase_duration),
                modifier = Modifier.size(32.dp),
                tint = if (canIncrease) TextMuted else Color.White.copy(alpha = 0.10f),
            )
        }

        // Large duration number
        Text(
            text = duration.toString(),
            fontFamily = Sora,
            fontWeight = FontWeight.ExtraLight,
            fontSize = 64.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
        )

        // "min" label
        Text(
            text = stringResource(R.string.min_label).uppercase(),
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextMuted,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Chevron down
        IconButton(
            onClick = onDecrease,
            enabled = canDecrease,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.decrease_duration),
                modifier = Modifier.size(32.dp),
                tint = if (canDecrease) TextMuted else Color.White.copy(alpha = 0.10f),
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

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(80.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.20f),
                    shape = CircleShape,
                ).background(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = CircleShape,
                ).clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
    ) {
        Icon(
            imageVector = HeroPlay,
            contentDescription = stringResource(R.string.start_session),
            modifier = Modifier.size(28.dp),
            tint = Color.White.copy(alpha = 0.80f),
        )
    }
}

private val HeroPlay: ImageVector =
    ImageVector
        .Builder(
            name = "HeroPlay",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5.25f, 5.653f)
                curveToRelative(0f, -0.856f, 0.917f, -1.398f, 1.667f, -0.986f)
                lineToRelative(11.54f, 6.347f)
                arcToRelative(1.125f, 1.125f, 0f, false, true, 0f, 1.972f)
                lineToRelative(-11.54f, 6.347f)
                arcToRelative(1.125f, 1.125f, 0f, false, true, -1.667f, -0.986f)
                verticalLineTo(5.653f)
                close()
            }
        }.build()

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun HomeContentPreview() {
    WaneTheme {
        HomeContent(
            uiState = HomeUiState(defaultDuration = 25),
            onStartSession = {},
            onIncreaseDuration = {},
            onDecreaseDuration = {},
            onSettings = {},
            onOpenAccessibilitySettings = {},
            onOpenNotificationSettings = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun HomeContentDurationBoundaryPreview() {
    WaneTheme {
        HomeContent(
            uiState = HomeUiState(defaultDuration = 120),
            onStartSession = {},
            onIncreaseDuration = {},
            onDecreaseDuration = {},
            onSettings = {},
            onOpenAccessibilitySettings = {},
            onOpenNotificationSettings = {},
        )
    }
}
