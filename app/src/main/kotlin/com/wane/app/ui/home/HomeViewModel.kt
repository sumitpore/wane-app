package com.wane.app.ui.home

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.data.repository.SessionRepository
import com.wane.app.service.SessionError
import com.wane.app.service.SessionManager
import com.wane.app.shared.SessionState
import com.wane.app.util.AccessibilityUtils
import com.wane.app.util.NotificationListenerUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentStreak: Int = 0,
    val defaultDuration: Int = 25,
    val isSessionActive: Boolean = false,
    val selectedThemeId: String = "calm_blue",
    val accessibilityDisabled: Boolean = false,
    val notificationListenerDisabled: Boolean = false,
)

sealed interface HomeUiEvent {
    data class StartSession(val durationMs: Long) : HomeUiEvent
    data class ChangeDuration(val minutes: Int) : HomeUiEvent
    data object IncreaseDuration : HomeUiEvent
    data object DecreaseDuration : HomeUiEvent
    data object DismissAccessibilityPrompt : HomeUiEvent
    data object OpenAccessibilitySettings : HomeUiEvent
    data object DismissNotificationPrompt : HomeUiEvent
    data object OpenNotificationSettings : HomeUiEvent
    data object RefreshPermissions : HomeUiEvent
}

sealed interface HomeEffect {
    data object NavigateToSession : HomeEffect
    data class ShowError(val error: SessionError) : HomeEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val sessionManager: SessionManager,
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _effects = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    private val _accessibilityDisabled = MutableStateFlow(false)
    private val _notificationListenerDisabled = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        sessionRepository.observeCurrentStreak(),
        preferencesRepository.observeDefaultDuration(),
        sessionManager.sessionState,
        preferencesRepository.observeSelectedThemeId(),
        _accessibilityDisabled,
        _notificationListenerDisabled,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val streak = values[0] as Int
        val duration = values[1] as Int
        val sessionState = values[2] as SessionState
        val themeId = values[3] as String
        val accessibilityDisabled = values[4] as Boolean
        val notificationListenerDisabled = values[5] as Boolean
        HomeUiState(
            currentStreak = streak,
            defaultDuration = duration,
            isSessionActive = sessionState is SessionState.Running,
            selectedThemeId = themeId,
            accessibilityDisabled = accessibilityDisabled,
            notificationListenerDisabled = notificationListenerDisabled,
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
            is HomeUiEvent.IncreaseDuration -> cycleDuration(forward = true)
            is HomeUiEvent.DecreaseDuration -> cycleDuration(forward = false)
            is HomeUiEvent.DismissAccessibilityPrompt -> _accessibilityDisabled.update { false }
            is HomeUiEvent.OpenAccessibilitySettings -> openAccessibilitySettings()
            is HomeUiEvent.DismissNotificationPrompt -> _notificationListenerDisabled.update { false }
            is HomeUiEvent.OpenNotificationSettings -> openNotificationSettings()
            is HomeUiEvent.RefreshPermissions -> refreshPermissions()
        }
    }

    private fun startSession(durationMs: Long) {
        if (!AccessibilityUtils.isAccessibilityServiceEnabled(application)) {
            _accessibilityDisabled.update { true }
            return
        }
        if (!NotificationListenerUtils.isNotificationListenerEnabled(application)) {
            _notificationListenerDisabled.update { true }
            return
        }
        viewModelScope.launch {
            val themeId = uiState.value.selectedThemeId
            sessionManager.startSession(durationMs, themeId)
            _effects.emit(HomeEffect.NavigateToSession)
        }
    }

    private fun refreshPermissions() {
        val accessibilityEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(application)
        if (accessibilityEnabled) {
            _accessibilityDisabled.update { false }
        }
        val notificationEnabled = NotificationListenerUtils.isNotificationListenerEnabled(application)
        if (notificationEnabled) {
            _notificationListenerDisabled.update { false }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        application.startActivity(intent)
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        application.startActivity(intent)
    }

    private fun changeDuration(minutes: Int) {
        val clamped = minutes.coerceIn(DURATION_OPTIONS.first(), DURATION_OPTIONS.last())
        viewModelScope.launch {
            preferencesRepository.setDefaultDuration(clamped)
        }
    }

    private fun cycleDuration(forward: Boolean) {
        val current = uiState.value.defaultDuration
        val currentIndex = DURATION_OPTIONS.indexOfFirst { it >= current }
            .takeIf { it >= 0 } ?: 0
        val nextIndex = if (forward) {
            (currentIndex + 1).coerceAtMost(DURATION_OPTIONS.lastIndex)
        } else {
            (currentIndex - 1).coerceAtLeast(0)
        }
        viewModelScope.launch {
            preferencesRepository.setDefaultDuration(DURATION_OPTIONS[nextIndex])
        }
    }

    private fun collectErrorEvents() {
        viewModelScope.launch {
            sessionManager.errorEvents.collect { error ->
                _effects.emit(HomeEffect.ShowError(error))
            }
        }
    }

    companion object {
        val DURATION_OPTIONS = listOf(5, 10, 15, 20, 25, 30, 45, 60, 90, 120)
    }
}
