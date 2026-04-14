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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Sms
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
import androidx.compose.ui.graphics.vector.ImageVector
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
        viewModel.onEvent(SessionUiEvent.ShowExitSheet)
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
            modifier = Modifier
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
                onEnd = { viewModel.onEvent(SessionUiEvent.ShowExitSheet) },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
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
            icon = Icons.Outlined.Call,
            label = stringResource(R.string.session_phone),
            onClick = onPhone,
        )

        ToolbarIconButton(
            icon = Icons.Outlined.Contacts,
            label = stringResource(R.string.session_contacts),
            onClick = onContacts,
        )

        ToolbarIconButton(
            icon = Icons.Outlined.Sms,
            label = stringResource(R.string.session_sms),
            onClick = onSms,
        )

        ToolbarIconButton(
            icon = Icons.Outlined.Close,
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
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceGlass),
            colors = IconButtonDefaults.iconButtonColors(
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
            modifier = Modifier
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
                text = "\"$exitPhrase\"",
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
                textStyle = WaneTypography.bodyLarge.copy(
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
                colors = OutlinedTextFieldDefaults.colors(
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
        modifier = Modifier
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
