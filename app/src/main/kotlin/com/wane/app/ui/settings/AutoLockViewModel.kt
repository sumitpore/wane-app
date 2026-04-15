package com.wane.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.shared.AutoLockConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AutoLockUiState(
    val config: AutoLockConfig = AutoLockConfig(),
)

sealed interface AutoLockUiEvent {
    data class SetEnabled(
        val enabled: Boolean,
    ) : AutoLockUiEvent

    data class SetDuration(
        val minutes: Int,
    ) : AutoLockUiEvent

    data class SetGracePeriod(
        val seconds: Int,
    ) : AutoLockUiEvent

    data class SetSkipWindow(
        val startHour: Int?,
        val startMinute: Int?,
        val endHour: Int?,
        val endMinute: Int?,
    ) : AutoLockUiEvent

    data class SetSkipWhileCharging(
        val skip: Boolean,
    ) : AutoLockUiEvent
}

@HiltViewModel
class AutoLockViewModel
    @Inject
    constructor(
        private val preferencesRepository: PreferencesRepository,
    ) : ViewModel() {
        val uiState: StateFlow<AutoLockUiState> =
            preferencesRepository
                .observeAutoLockConfig()
                .map { AutoLockUiState(config = it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = AutoLockUiState(),
                )

        fun onEvent(event: AutoLockUiEvent) {
            val current = uiState.value.config
            val updated =
                when (event) {
                    is AutoLockUiEvent.SetEnabled -> {
                        current.copy(enabled = event.enabled)
                    }

                    is AutoLockUiEvent.SetDuration -> {
                        current.copy(durationMinutes = event.minutes.coerceIn(5, 120))
                    }

                    is AutoLockUiEvent.SetGracePeriod -> {
                        current.copy(gracePeriodSeconds = event.seconds.coerceIn(0, 60))
                    }

                    is AutoLockUiEvent.SetSkipWindow -> {
                        current.copy(
                            skipStartHour = event.startHour,
                            skipStartMinute = event.startMinute,
                            skipEndHour = event.endHour,
                            skipEndMinute = event.endMinute,
                        )
                    }

                    is AutoLockUiEvent.SetSkipWhileCharging -> {
                        current.copy(skipWhileCharging = event.skip)
                    }
                }
            viewModelScope.launch {
                preferencesRepository.setAutoLockConfig(updated)
            }
        }
    }
