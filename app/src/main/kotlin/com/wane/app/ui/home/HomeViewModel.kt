package com.wane.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.data.repository.SessionRepository
import com.wane.app.service.SessionError
import com.wane.app.service.SessionManager
import com.wane.app.shared.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentStreak: Int = 0,
    val defaultDuration: Int = 25,
    val isSessionActive: Boolean = false,
    val selectedThemeId: String = "calm_blue",
)

sealed interface HomeUiEvent {
    data class StartSession(val durationMs: Long) : HomeUiEvent
    data class ChangeDuration(val minutes: Int) : HomeUiEvent
}

sealed interface HomeEffect {
    data object NavigateToSession : HomeEffect
    data class ShowError(val error: SessionError) : HomeEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _effects = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        sessionRepository.observeCurrentStreak(),
        preferencesRepository.observeDefaultDuration(),
        sessionManager.sessionState,
        preferencesRepository.observeSelectedThemeId(),
    ) { streak, duration, sessionState, themeId ->
        HomeUiState(
            currentStreak = streak,
            defaultDuration = duration,
            isSessionActive = sessionState is SessionState.Running,
            selectedThemeId = themeId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        collectErrorEvents()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.StartSession -> startSession(event.durationMs)
            is HomeUiEvent.ChangeDuration -> changeDuration(event.minutes)
        }
    }

    private fun startSession(durationMs: Long) {
        viewModelScope.launch {
            val themeId = uiState.value.selectedThemeId
            sessionManager.startSession(durationMs, themeId)
            _effects.emit(HomeEffect.NavigateToSession)
        }
    }

    private fun changeDuration(minutes: Int) {
        val clamped = minutes.coerceIn(5, 120)
        viewModelScope.launch {
            preferencesRepository.setDefaultDuration(clamped)
        }
    }

    private fun collectErrorEvents() {
        viewModelScope.launch {
            sessionManager.errorEvents.collect { error ->
                _effects.emit(HomeEffect.ShowError(error))
            }
        }
    }
}
