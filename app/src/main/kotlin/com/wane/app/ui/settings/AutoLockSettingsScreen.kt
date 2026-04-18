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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wane.app.R
import com.wane.app.ui.theme.AccentPrimary
import com.wane.app.ui.theme.BackgroundSettings
import com.wane.app.ui.theme.SurfaceDim
import com.wane.app.ui.theme.SurfaceGlass
import com.wane.app.ui.theme.TextMuted
import com.wane.app.ui.theme.TextPrimary
import com.wane.app.ui.theme.TextSecondary
import com.wane.app.ui.theme.TextStatus
import com.wane.app.ui.theme.WaneTypography
import kotlin.math.roundToInt

@Composable
fun AutoLockSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AutoLockViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val config = uiState.config

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BackgroundSettings)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        AutoLockTopBar(onBack = onNavigateBack)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            AutoLockCard {
                AutoLockToggleRow(
                    label = stringResource(R.string.auto_lock_enabled),
                    checked = config.enabled,
                    onCheckedChange = { viewModel.onEvent(AutoLockUiEvent.SetEnabled(it)) },
                )
            }

            AutoLockCard {
                SliderRow(
                    label = stringResource(R.string.grace_period),
                    value = config.gracePeriodSeconds.toFloat(),
                    valueLabel = stringResource(R.string.autolock_seconds_format, config.gracePeriodSeconds),
                    range = 0f..60f,
                    steps = 11,
                    onValueChange = { viewModel.onEvent(AutoLockUiEvent.SetGracePeriod(it.roundToInt())) },
                    enabled = config.enabled,
                )
            }

            SkipWindowSection(config = config, viewModel = viewModel)

            AutoLockCard {
                AutoLockToggleRow(
                    label = stringResource(R.string.skip_while_charging),
                    checked = config.skipWhileCharging,
                    onCheckedChange = { viewModel.onEvent(AutoLockUiEvent.SetSkipWhileCharging(it)) },
                    enabled = config.enabled,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AutoLockTopBar(onBack: () -> Unit) {
    Row(
        modifier =
            Modifier
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
            text = stringResource(R.string.auto_lock_settings_title),
            style = WaneTypography.headlineMedium,
            color = TextPrimary,
        )
    }
}

@Composable
private fun AutoLockCard(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceGlass)
                .animateContentSize(animationSpec = spring(stiffness = 100f, dampingRatio = 0.85f)),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun AutoLockToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = WaneTypography.bodyLarge,
            color = if (enabled) TextPrimary else TextMuted,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = AccentPrimary,
                    checkedTrackColor = AccentPrimary.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextStatus,
                    uncheckedTrackColor = SurfaceDim,
                ),
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = WaneTypography.bodyLarge,
                color = if (enabled) TextPrimary else TextMuted,
            )
            Text(
                text = valueLabel,
                style = WaneTypography.bodyMedium,
                color = if (enabled) AccentPrimary else TextMuted,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            enabled = enabled,
            colors =
                SliderDefaults.colors(
                    thumbColor = AccentPrimary,
                    activeTrackColor = AccentPrimary,
                    inactiveTrackColor = SurfaceDim,
                    disabledThumbColor = TextMuted,
                    disabledActiveTrackColor = TextMuted,
                    disabledInactiveTrackColor = SurfaceDim,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SkipWindowSection(
    config: com.wane.app.shared.AutoLockConfig,
    viewModel: AutoLockViewModel,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val hasWindow = config.skipStartHour != null

    AutoLockCard {
        SkipWindowRow(
            hasWindow = hasWindow,
            startHour = config.skipStartHour,
            startMinute = config.skipStartMinute,
            endHour = config.skipEndHour,
            endMinute = config.skipEndMinute,
            enabled = config.enabled,
            onToggle = { enabled ->
                if (enabled) {
                    viewModel.onEvent(AutoLockUiEvent.SetSkipWindow(22, 0, 7, 0))
                } else {
                    viewModel.onEvent(AutoLockUiEvent.SetSkipWindow(null, null, null, null))
                }
            },
            onStartTimeClick = { showStartPicker = true },
            onEndTimeClick = { showEndPicker = true },
        )
    }

    if (showStartPicker && hasWindow) {
        WaneTimePickerDialog(
            initialHour = config.skipStartHour!!,
            initialMinute = config.skipStartMinute ?: 0,
            onConfirm = { h, m ->
                viewModel.onEvent(
                    AutoLockUiEvent.SetSkipWindow(h, m, config.skipEndHour, config.skipEndMinute),
                )
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false },
        )
    }

    if (showEndPicker && hasWindow) {
        WaneTimePickerDialog(
            initialHour = config.skipEndHour!!,
            initialMinute = config.skipEndMinute ?: 0,
            onConfirm = { h, m ->
                viewModel.onEvent(
                    AutoLockUiEvent.SetSkipWindow(config.skipStartHour, config.skipStartMinute, h, m),
                )
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false },
        )
    }
}

@Composable
private fun SkipWindowRow(
    hasWindow: Boolean,
    startHour: Int?,
    startMinute: Int?,
    endHour: Int?,
    endMinute: Int?,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.skip_window),
                style = WaneTypography.bodyLarge,
                color = if (enabled) TextPrimary else TextMuted,
            )
            Switch(
                checked = hasWindow,
                onCheckedChange = onToggle,
                enabled = enabled,
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = AccentPrimary,
                        checkedTrackColor = AccentPrimary.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextStatus,
                        uncheckedTrackColor = SurfaceDim,
                    ),
            )
        }
        if (hasWindow && startHour != null && endHour != null) {
            Spacer(modifier = Modifier.height(8.dp))
            SkipWindowTimeChips(
                startHour = startHour,
                startMinute = startMinute ?: 0,
                endHour = endHour,
                endMinute = endMinute ?: 0,
                enabled = enabled,
                onStartClick = onStartTimeClick,
                onEndClick = onEndTimeClick,
            )
        }
    }
}

@Composable
private fun SkipWindowTimeChips(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    enabled: Boolean,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeChip(startHour, startMinute, stringResource(R.string.skip_window_start), enabled, onStartClick)
        Text(
            text = "–",
            style = WaneTypography.bodyMedium,
            color = TextStatus,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        TimeChip(endHour, endMinute, stringResource(R.string.skip_window_end), enabled, onEndClick)
    }
}

@Composable
private fun TimeChip(
    hour: Int,
    minute: Int,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDim)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = WaneTypography.labelSmall,
            color = if (enabled) TextStatus else TextMuted,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "%02d:%02d".format(hour, minute),
            style = WaneTypography.bodyLarge,
            color = if (enabled) AccentPrimary else TextMuted,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaneTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state =
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(BackgroundSettings)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TimePicker(
                state = state,
                colors =
                    TimePickerDefaults.colors(
                        clockDialColor = SurfaceDim,
                        selectorColor = AccentPrimary,
                        containerColor = BackgroundSettings,
                        clockDialSelectedContentColor = TextPrimary,
                        clockDialUnselectedContentColor = TextSecondary,
                        timeSelectorSelectedContainerColor = AccentPrimary.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = AccentPrimary,
                        timeSelectorUnselectedContainerColor = SurfaceDim,
                        timeSelectorUnselectedContentColor = TextSecondary,
                    ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = TextStatus)
                }
                TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                    Text(stringResource(R.string.done), color = AccentPrimary)
                }
            }
        }
    }
}
