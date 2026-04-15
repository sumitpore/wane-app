package com.wane.app.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wane.app.R
import com.wane.app.animation.WaterCanvas
import com.wane.app.ui.components.WaneButton
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.BackgroundDeep
import com.wane.app.ui.theme.Crystalline
import com.wane.app.ui.theme.SurfaceGlass
import com.wane.app.ui.theme.TextMuted
import com.wane.app.ui.theme.TextPrimary
import com.wane.app.ui.theme.TextSecondary
import com.wane.app.ui.theme.TextStatus
import com.wane.app.ui.theme.WaneTypography
import com.wane.app.util.IntentHelpers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    onSessionEnd: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        viewModel.onEvent(SessionUiEvent.ShowGraduatedExit)
    }

    LaunchedEffect(uiState.shouldNavigateHome) {
        if (uiState.shouldNavigateHome) {
            onSessionEnd()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WaterCanvas(
            waterLevel = uiState.waterLevel,
            tiltState = uiState.tiltState,
            themeVisuals = uiState.themeVisuals,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.8f))

            BottomToolbar(
                onPhone = { IntentHelpers.openDialer(context) },
                onContacts = { IntentHelpers.openContacts(context) },
                onSms = { IntentHelpers.openSms(context) },
                onEnd = { viewModel.onEvent(SessionUiEvent.ShowGraduatedExit) },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (uiState.isGraduatedExitVisible) {
        GraduatedExitSheet(
            onKeepGoing = { viewModel.onEvent(SessionUiEvent.ExtendSession) },
            onLeave = { viewModel.onEvent(SessionUiEvent.ProceedToEmergencyExit) },
            onDismiss = { viewModel.onEvent(SessionUiEvent.DismissGraduatedExit) },
        )
    }

    if (uiState.isExitSheetVisible) {
        EmergencyExitSheet(
            exitInput = uiState.exitInput,
            exitPhrase = uiState.exitPhrase,
            onInputChange = { viewModel.onEvent(SessionUiEvent.UpdateExitInput(it)) },
            onConfirm = { viewModel.onEvent(SessionUiEvent.ConfirmEmergencyExit) },
            onDismiss = { viewModel.onEvent(SessionUiEvent.DismissExitSheet) },
        )
    }

    AnimatedVisibility(
        visible = uiState.isSessionComplete,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        SessionCompleteOverlay(
            completedDurationMs = uiState.completedDurationMs,
            onDone = {
                viewModel.onEvent(SessionUiEvent.ConfirmComplete)
                onSessionEnd()
            },
        )
    }
}

@Composable
private fun BottomToolbar(
    onPhone: () -> Unit,
    onContacts: () -> Unit,
    onSms: () -> Unit,
    onEnd: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarIconButton(
            icon = HeroPhone,
            label = stringResource(R.string.session_phone),
            onClick = onPhone,
        )

        ToolbarIconButton(
            icon = HeroContacts,
            label = stringResource(R.string.session_contacts),
            onClick = onContacts,
        )

        ToolbarIconButton(
            icon = HeroSms,
            label = stringResource(R.string.session_sms),
            onClick = onSms,
        )

        ToolbarIconButton(
            icon = HeroXMark,
            label = stringResource(R.string.end_session),
            onClick = onEnd,
            contentColor = TextStatus,
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    contentColor: Color = TextSecondary,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(
            onClick = onClick,
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceGlass),
            colors =
                IconButtonDefaults.iconButtonColors(
                    contentColor = contentColor,
                ),
        ) {
            Icon(icon, contentDescription = label)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = WaneTypography.labelSmall,
            color = TextMuted,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmergencyExitSheet(
    exitInput: String,
    exitPhrase: String,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDeep,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.emergency_exit_title),
                style = WaneTypography.headlineMedium,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.emergency_exit_instruction),
                style = WaneTypography.bodyMedium,
                color = TextStatus,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exitPhrase,
                style = WaneTypography.bodyLarge,
                color = AccentPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = exitInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle =
                    WaneTypography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        color = TextPrimary,
                    ),
                placeholder = {
                    Text(
                        text = exitPhrase,
                        style = WaneTypography.bodyLarge.copy(textAlign = TextAlign.Center),
                        color = TextMuted,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = SurfaceGlass,
                        cursorColor = AccentPrimary,
                    ),
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = WaneTypography.labelLarge,
                        color = TextStatus,
                    )
                }

                WaneButton(
                    text = stringResource(R.string.end_session),
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    enabled = exitInput.equals(exitPhrase, ignoreCase = true),
                    containerColor = Color(0xFFE04848),
                )
            }
        }
    }
}

@Composable
private fun SessionCompleteOverlay(
    completedDurationMs: Long,
    onDone: () -> Unit,
) {
    val completedMinutes = (completedDurationMs / 60_000).toInt()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BackgroundDeep.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp),
        ) {
            Text(
                text = stringResource(R.string.session_complete_title),
                style = WaneTypography.headlineLarge,
                color = Crystalline,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.session_complete_message, completedMinutes),
                style = WaneTypography.bodyLarge,
                color = TextStatus,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            WaneButton(
                text = stringResource(R.string.done),
                onClick = onDone,
                modifier = Modifier.width(200.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GraduatedExitSheet(
    onKeepGoing: () -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDeep,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.graduated_exit_title),
                style = WaneTypography.headlineMedium,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onLeave,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.graduated_exit_leave),
                        style = WaneTypography.labelLarge,
                        color = TextStatus,
                    )
                }

                Button(
                    onClick = onKeepGoing,
                    modifier = Modifier.weight(1f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = AccentPrimary,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.graduated_exit_keep_going),
                        style = WaneTypography.labelLarge,
                    )
                }
            }
        }
    }
}

private val HeroPhone: ImageVector =
    ImageVector
        .Builder(
            name = "HeroPhone",
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
                moveTo(2.25f, 6.75f)
                curveToRelative(0f, 8.284f, 6.716f, 15f, 15f, 15f)
                horizontalLineToRelative(2.25f)
                arcToRelative(2.25f, 2.25f, 0f, false, false, 2.25f, -2.25f)
                verticalLineToRelative(-1.372f)
                curveToRelative(0f, -0.516f, -0.351f, -0.966f, -0.852f, -1.091f)
                lineToRelative(-4.423f, -1.106f)
                curveToRelative(-0.44f, -0.11f, -0.902f, 0.055f, -1.173f, 0.417f)
                lineToRelative(-0.97f, 1.293f)
                curveToRelative(-0.282f, 0.376f, -0.769f, 0.542f, -1.21f, 0.38f)
                arcToRelative(12.035f, 12.035f, 0f, false, true, -7.143f, -7.143f)
                curveToRelative(-0.162f, -0.441f, 0.004f, -0.928f, 0.38f, -1.21f)
                lineToRelative(1.293f, -0.97f)
                curveToRelative(0.363f, -0.271f, 0.527f, -0.734f, 0.417f, -1.173f)
                lineTo(6.963f, 3.102f)
                arcToRelative(1.125f, 1.125f, 0f, false, false, -1.091f, -0.852f)
                horizontalLineTo(4.5f)
                arcTo(2.25f, 2.25f, 0f, false, false, 2.25f, 4.5f)
                verticalLineToRelative(2.25f)
                close()
            }
        }.build()

private val HeroSms: ImageVector =
    ImageVector
        .Builder(
            name = "HeroSms",
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
                moveTo(20.25f, 8.511f)
                curveToRelative(0.884f, 0.284f, 1.5f, 1.128f, 1.5f, 2.097f)
                verticalLineToRelative(4.286f)
                curveToRelative(0f, 1.136f, -0.847f, 2.1f, -1.98f, 2.193f)
                curveToRelative(-0.34f, 0.027f, -0.68f, 0.052f, -1.02f, 0.072f)
                verticalLineToRelative(3.091f)
                lineToRelative(-3f, -3f)
                curveToRelative(-1.354f, 0f, -2.694f, -0.055f, -4.02f, -0.163f)
                arcToRelative(2.115f, 2.115f, 0f, false, true, -0.825f, -0.242f)
                moveToRelative(9.345f, -8.334f)
                arcToRelative(2.126f, 2.126f, 0f, false, false, -0.476f, -0.095f)
                arcToRelative(48.64f, 48.64f, 0f, false, false, -8.048f, 0f)
                curveToRelative(-1.131f, 0.094f, -1.976f, 1.057f, -1.976f, 2.192f)
                verticalLineToRelative(4.286f)
                curveToRelative(0f, 0.837f, 0.46f, 1.58f, 1.155f, 1.951f)
                moveToRelative(9.345f, -8.334f)
                verticalLineTo(6.637f)
                curveToRelative(0f, -1.621f, -1.152f, -3.026f, -2.76f, -3.235f)
                arcTo(48.455f, 48.455f, 0f, false, false, 11.25f, 3f)
                curveToRelative(-2.115f, 0f, -4.198f, 0.137f, -6.24f, 0.402f)
                curveToRelative(-1.608f, 0.209f, -2.76f, 1.614f, -2.76f, 3.235f)
                verticalLineToRelative(6.226f)
                curveToRelative(0f, 1.621f, 1.152f, 3.026f, 2.76f, 3.235f)
                curveToRelative(0.577f, 0.075f, 1.157f, 0.14f, 1.74f, 0.194f)
                verticalLineTo(21f)
                lineToRelative(4.155f, -4.155f)
            }
        }.build()

private val HeroContacts: ImageVector =
    ImageVector
        .Builder(
            name = "HeroContacts",
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
                moveTo(18f, 18.72f)
                arcToRelative(9.094f, 9.094f, 0f, false, false, 3.741f, -0.479f)
                arcToRelative(3f, 3f, 0f, false, false, -4.682f, -2.72f)
                moveToRelative(0.94f, 3.198f)
                lineToRelative(0.001f, 0.031f)
                curveToRelative(0f, 0.225f, -0.012f, 0.447f, -0.037f, 0.666f)
                arcTo(11.944f, 11.944f, 0f, false, true, 12f, 21f)
                curveToRelative(-2.17f, 0f, -4.207f, -0.576f, -5.963f, -1.584f)
                arcTo(6.062f, 6.062f, 0f, false, true, 6f, 18.719f)
                moveToRelative(12f, 0f)
                arcToRelative(5.971f, 5.971f, 0f, false, false, -0.941f, -3.197f)
                moveToRelative(0f, 0f)
                arcTo(5.995f, 5.995f, 0f, false, false, 12f, 12.75f)
                arcToRelative(5.995f, 5.995f, 0f, false, false, -5.058f, 2.772f)
                moveToRelative(0f, 0f)
                arcToRelative(3f, 3f, 0f, false, false, -4.681f, 2.72f)
                arcToRelative(8.986f, 8.986f, 0f, false, false, 3.74f, 0.477f)
                moveToRelative(0.94f, -3.197f)
                arcToRelative(5.971f, 5.971f, 0f, false, false, -0.94f, 3.197f)
                moveTo(15f, 6.75f)
                arcToRelative(3f, 3f, 0f, true, true, -6f, 0f)
                arcToRelative(3f, 3f, 0f, false, true, 6f, 0f)
                close()
                moveTo(21f, 9.75f)
                arcToRelative(2.25f, 2.25f, 0f, true, true, -4.5f, 0f)
                arcToRelative(2.25f, 2.25f, 0f, false, true, 4.5f, 0f)
                close()
                moveTo(7.5f, 9.75f)
                arcToRelative(2.25f, 2.25f, 0f, true, true, -4.5f, 0f)
                arcToRelative(2.25f, 2.25f, 0f, false, true, 4.5f, 0f)
                close()
            }
        }.build()

private val HeroXMark: ImageVector =
    ImageVector
        .Builder(
            name = "HeroXMark",
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
                moveTo(6f, 18f)
                lineTo(18f, 6f)
                moveTo(6f, 6f)
                lineToRelative(12f, 12f)
            }
        }.build()
