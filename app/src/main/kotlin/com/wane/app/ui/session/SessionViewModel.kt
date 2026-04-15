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
    val isExitSheetVisible: Boolean = false,
    val exitInput: String = "",
    val exitPhrase: String = "",
    val isSessionComplete: Boolean = false,
    val completedDurationMs: Long = 0L,
    val shouldNavigateHome: Boolean = false,
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
                isExitSheetVisible = local.isExitSheetVisible,
                exitInput = local.exitInput,
                exitPhrase = local.exitPhrase,
                isSessionComplete = false,
                completedDurationMs = 0L,
            )
            is SessionState.Completing -> SessionUiState(
                waterLevel = 0f,
                tiltState = tilt,
                themeVisuals = visuals,
                isExitSheetVisible = false,
                exitInput = "",
                isSessionComplete = true,
                completedDurationMs = session.actualDurationMs,
            )
            is SessionState.EmergencyExit -> SessionUiState(
                themeVisuals = visuals,
                tiltState = tilt,
                shouldNavigateHome = true,
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
                localState.update {
                    it.copy(
                        isExitSheetVisible = true,
                        exitInput = "",
                        exitPhrase = EXIT_PHRASES.random(),
                    )
                }
            SessionUiEvent.DismissExitSheet ->
                localState.update { it.copy(isExitSheetVisible = false, exitInput = "") }
            is SessionUiEvent.UpdateExitInput ->
                localState.update { it.copy(exitInput = event.text) }
            SessionUiEvent.ConfirmEmergencyExit -> {
                val local = localState.value
                if (local.exitInput.equals(local.exitPhrase, ignoreCase = true)) {
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
        val exitPhrase: String = "",
    )

    companion object {
        private val EXIT_PHRASES = listOf(
            "I am blessed",
            "I feel alive",
            "I am worthy",
            "I am strong",
            "I am loved",
            "I am brave",
            "I am at peace",
            "I am grateful",
            "I shine bright",
            "I am radiant",
        )
    }
}
