package com.wane.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wane.app.animation.TiltSensorManager
import com.wane.app.animation.WaterThemeCatalog
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.service.SessionManager
import com.wane.app.shared.SessionState
import com.wane.app.shared.TiltState
import com.wane.app.shared.WaterThemeVisuals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val waterLevel: Float = 1.0f,
    val tiltState: TiltState = TiltState.Unavailable,
    val themeVisuals: WaterThemeVisuals = WaterThemeCatalog.defaultVisuals,
    val remainingMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isExitSheetVisible: Boolean = false,
    val exitInput: String = "",
    val isSessionComplete: Boolean = false,
    val completedDurationMs: Long = 0L,
)

sealed interface SessionUiEvent {
    data object ShowExitSheet : SessionUiEvent
    data object DismissExitSheet : SessionUiEvent
    data class UpdateExitInput(val text: String) : SessionUiEvent
    data object ConfirmEmergencyExit : SessionUiEvent
    data object ConfirmComplete : SessionUiEvent
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val tiltSensorManager: TiltSensorManager,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val localState = MutableStateFlow(LocalSessionState())
    private val tiltState = MutableStateFlow<TiltState>(TiltState.Unavailable)

    val uiState: StateFlow<SessionUiState> = combine(
        sessionManager.sessionState,
        tiltState,
        preferencesRepository.observeSelectedThemeId(),
        localState,
    ) { session, tilt, themeId, local ->
        val visuals = WaterThemeCatalog.getVisuals(themeId) ?: WaterThemeCatalog.defaultVisuals
        when (session) {
            is SessionState.Running -> SessionUiState(
                waterLevel = session.waterLevel,
                tiltState = tilt,
                themeVisuals = visuals,
                remainingMs = session.remainingMs,
                totalDurationMs = session.totalDurationMs,
                isExitSheetVisible = local.isExitSheetVisible,
                exitInput = local.exitInput,
                isSessionComplete = false,
                completedDurationMs = 0L,
            )
            is SessionState.Completing -> SessionUiState(
                waterLevel = 0f,
                tiltState = tilt,
                themeVisuals = visuals,
                remainingMs = 0L,
                totalDurationMs = session.totalDurationMs,
                isExitSheetVisible = false,
                exitInput = "",
                isSessionComplete = true,
                completedDurationMs = session.actualDurationMs,
            )
            else -> SessionUiState(themeVisuals = visuals, tiltState = tilt)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SessionUiState(),
    )

    init {
        tiltSensorManager.start()
        collectTilt()
    }

    fun onEvent(event: SessionUiEvent) {
        when (event) {
            SessionUiEvent.ShowExitSheet ->
                localState.update { it.copy(isExitSheetVisible = true, exitInput = "") }
            SessionUiEvent.DismissExitSheet ->
                localState.update { it.copy(isExitSheetVisible = false, exitInput = "") }
            is SessionUiEvent.UpdateExitInput ->
                localState.update { it.copy(exitInput = event.text) }
            SessionUiEvent.ConfirmEmergencyExit -> {
                if (localState.value.exitInput.equals(EXIT_KEYWORD, ignoreCase = false)) {
                    sessionManager.requestEmergencyExit()
                    localState.update { it.copy(isExitSheetVisible = false, exitInput = "") }
                }
            }
            SessionUiEvent.ConfirmComplete ->
                sessionManager.confirmSessionComplete()
        }
    }

    private fun collectTilt() {
        viewModelScope.launch {
            tiltSensorManager.tiltFlow.collect { tilt ->
                tiltState.value = tilt
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tiltSensorManager.stop()
    }

    private data class LocalSessionState(
        val isExitSheetVisible: Boolean = false,
        val exitInput: String = "",
    )

    companion object {
        private const val EXIT_KEYWORD = "EXIT"
    }
}
