package com.wane.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.data.repository.SessionRepository
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
    val streakInfo: StreakInfo = StreakInfo(0, 0, 0, 0L),
    val showClearConfirmation: Boolean = false,
)

sealed interface SettingsUiEvent {
    data object ShowClearConfirmation : SettingsUiEvent

    data object DismissClearConfirmation : SettingsUiEvent

    data object ConfirmClearSessions : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: PreferencesRepository,
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val localState = MutableStateFlow(LocalSettingsState())

        val uiState: StateFlow<SettingsUiState> =
            combine(
                preferencesRepository.observeDefaultDuration(),
                sessionRepository.observeStreakInfo(),
                localState,
            ) { duration, streak, local ->
                SettingsUiState(
                    defaultDuration = duration,
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
                SettingsUiEvent.ShowClearConfirmation -> {
                    localState.update { it.copy(showClearConfirmation = true) }
                }

                SettingsUiEvent.DismissClearConfirmation -> {
                    localState.update { it.copy(showClearConfirmation = false) }
                }

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
