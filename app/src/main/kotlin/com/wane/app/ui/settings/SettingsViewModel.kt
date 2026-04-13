package com.wane.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.data.repository.SessionRepository
import com.wane.app.data.repository.ThemeRepository
import com.wane.app.shared.StreakInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val defaultDuration: Int = 25,
    val ambientSounds: Boolean = false,
    val hapticFeedback: Boolean = true,
    val emergencyContacts: List<String> = emptyList(),
    val streakInfo: StreakInfo = StreakInfo(0, 0, 0, 0L),
    val showClearConfirmation: Boolean = false,
)

sealed interface SettingsUiEvent {
    data class SetDefaultDuration(val minutes: Int) : SettingsUiEvent
    data class SetAmbientSounds(val enabled: Boolean) : SettingsUiEvent
    data class SetHapticFeedback(val enabled: Boolean) : SettingsUiEvent
    data object ShowClearConfirmation : SettingsUiEvent
    data object DismissClearConfirmation : SettingsUiEvent
    data object ConfirmClearSessions : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val themeRepository: ThemeRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val localState = MutableStateFlow(LocalSettingsState())

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.observeDefaultDuration(),
        preferencesRepository.observeAmbientSoundsEnabled(),
        preferencesRepository.observeHapticFeedbackEnabled(),
        preferencesRepository.observeEmergencyContacts(),
        sessionRepository.observeStreakInfo(),
        localState,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val duration = values[0] as Int
        val ambient = values[1] as Boolean
        val haptic = values[2] as Boolean
        val contacts = values[3] as List<String>
        val streak = values[4] as StreakInfo
        val local = values[5] as LocalSettingsState

        SettingsUiState(
            defaultDuration = duration,
            ambientSounds = ambient,
            hapticFeedback = haptic,
            emergencyContacts = contacts,
            streakInfo = streak,
            showClearConfirmation = local.showClearConfirmation,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.SetDefaultDuration -> viewModelScope.launch {
                preferencesRepository.setDefaultDuration(event.minutes.coerceIn(5, 120))
            }
            is SettingsUiEvent.SetAmbientSounds -> viewModelScope.launch {
                preferencesRepository.setAmbientSoundsEnabled(event.enabled)
            }
            is SettingsUiEvent.SetHapticFeedback -> viewModelScope.launch {
                preferencesRepository.setHapticFeedbackEnabled(event.enabled)
            }
            SettingsUiEvent.ShowClearConfirmation ->
                localState.update { it.copy(showClearConfirmation = true) }
            SettingsUiEvent.DismissClearConfirmation ->
                localState.update { it.copy(showClearConfirmation = false) }
            SettingsUiEvent.ConfirmClearSessions -> {
                localState.update { it.copy(showClearConfirmation = false) }
                viewModelScope.launch { sessionRepository.clearAllSessions() }
            }
        }
    }

    private data class LocalSettingsState(
        val showClearConfirmation: Boolean = false,
    )
}
