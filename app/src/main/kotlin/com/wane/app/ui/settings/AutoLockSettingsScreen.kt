package com.wane.app.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSettings)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        AutoLockTopBar(onBack = onNavigateBack)

        Column(
            modifier = Modifier
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
                    label = stringResource(R.string.auto_lock_duration),
                    value = config.durationMinutes.toFloat(),
                    valueLabel = stringResource(R.string.minutes_format, config.durationMinutes),
                    range = 5f..120f,
                    steps = 22,
                    onValueChange = { viewModel.onEvent(AutoLockUiEvent.SetDuration(it.roundToInt())) },
                    enabled = config.enabled,
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

            AutoLockCard {
                SkipWindowRow(
                    hasWindow = config.skipStartHour != null,
                    startHour = config.skipStartHour,
                    startMinute = config.skipStartMinute,
                    endHour = config.skipEndHour,
                    endMinute = config.skipEndMinute,
                    enabled = config.enabled,
                    onToggle = { enabled ->
                        if (enabled) {
                            viewModel.onEvent(
                                AutoLockUiEvent.SetSkipWindow(22, 0, 7, 0),
                            )
                        } else {
                            viewModel.onEvent(
                                AutoLockUiEvent.SetSkipWindow(null, null, null, null),
                            )
                        }
                    },
                )
            }

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
            text = stringResource(R.string.auto_lock_settings_title),
            style = WaneTypography.headlineMedium,
            color = TextPrimary,
        )
    }
}

@Composable
private fun AutoLockCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
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
        modifier = Modifier
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
            colors = SwitchDefaults.colors(
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
        modifier = Modifier
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
            colors = SliderDefaults.colors(
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
private fun SkipWindowRow(
    hasWindow: Boolean,
    startHour: Int?,
    startMinute: Int?,
    endHour: Int?,
    endMinute: Int?,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
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
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentPrimary,
                    checkedTrackColor = AccentPrimary.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextStatus,
                    uncheckedTrackColor = SurfaceDim,
                ),
            )
        }

        if (hasWindow && startHour != null && endHour != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.autolock_skip_window_range,
                    startHour,
                    startMinute ?: 0,
                    endHour,
                    endMinute ?: 0,
                ),
                style = WaneTypography.bodyMedium,
                color = TextStatus,
            )
        }
    }
}
