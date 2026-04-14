package com.wane.app.ui.settings

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wane.app.R
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.BackgroundSettings
import com.wane.app.ui.theme.SurfaceGlass
import com.wane.app.ui.theme.TextMuted
import com.wane.app.ui.theme.TextPrimary
import com.wane.app.ui.theme.TextSecondary
import com.wane.app.ui.theme.TextStatus
import com.wane.app.ui.theme.WaneTypography

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAutoLock: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSettings)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        SettingsTopBar(onBack = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SettingsSection(title = stringResource(R.string.focus_section)) {
                SettingsRowValue(
                    label = stringResource(R.string.default_duration),
                    value = stringResource(R.string.minutes_format, uiState.defaultDuration),
                )
            }

            SettingsSection(title = stringResource(R.string.blocking_section)) {
                SettingsRowNavigation(
                    label = stringResource(R.string.auto_lock),
                    onClick = onNavigateToAutoLock,
                )
            }

            SettingsSection(title = stringResource(R.string.data_section)) {
                SettingsRowValue(
                    label = stringResource(R.string.settings_total_sessions),
                    value = uiState.streakInfo.totalSessions.toString(),
                )
                SettingsRowValue(
                    label = stringResource(R.string.settings_total_focus_time),
                    value = stringResource(
                        R.string.settings_hours_format,
                        uiState.streakInfo.totalMinutes / 60,
                    ),
                )
                SettingsRowDanger(
                    label = stringResource(R.string.clear_sessions),
                    onClick = { viewModel.onEvent(SettingsUiEvent.ShowClearConfirmation) },
                )
            }

            SettingsSection(title = stringResource(R.string.about_section)) {
                SettingsRowValue(
                    label = stringResource(R.string.settings_version),
                    value = stringResource(R.string.settings_version_value),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingsUiEvent.DismissClearConfirmation) },
            title = {
                Text(
                    text = stringResource(R.string.clear_sessions),
                    style = WaneTypography.headlineMedium,
                    color = TextPrimary,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.clear_sessions_confirm),
                    style = WaneTypography.bodyMedium,
                    color = TextStatus,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(SettingsUiEvent.ConfirmClearSessions) },
                ) {
                    Text(
                        text = stringResource(R.string.settings_clear),
                        style = WaneTypography.labelLarge,
                        color = AccentPrimary,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(SettingsUiEvent.DismissClearConfirmation) },
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = WaneTypography.labelLarge,
                        color = TextStatus,
                    )
                }
            },
            containerColor = BackgroundSettings,
        )
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.settings_back),
                tint = TextSecondary,
            )
        }
        Text(
            text = stringResource(R.string.settings_title),
            style = WaneTypography.headlineMedium,
            color = TextPrimary,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title.uppercase(),
            style = WaneTypography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceGlass)
                .animateContentSize(animationSpec = spring(stiffness = 100f, dampingRatio = 0.85f)),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRowValue(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = WaneTypography.bodyLarge,
            color = TextPrimary,
        )
        Text(
            text = value,
            style = WaneTypography.bodyMedium,
            color = TextStatus,
        )
    }
}

@Composable
private fun SettingsRowNavigation(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = WaneTypography.bodyLarge,
            color = TextPrimary,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsRowDanger(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = label,
            style = WaneTypography.bodyLarge,
            color = AccentPrimary,
        )
    }
}
