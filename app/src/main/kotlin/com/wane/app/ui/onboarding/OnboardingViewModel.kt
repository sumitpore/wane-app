package com.wane.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.shared.AutoLockConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val selectedDuration: Int = 25,
    val autoLockEnabled: Boolean = false,
)

sealed interface OnboardingUiEvent {
    data object NextPage : OnboardingUiEvent
    data object PreviousPage : OnboardingUiEvent
    data class SetDuration(val minutes: Int) : OnboardingUiEvent
    data class ToggleAutoLock(val enabled: Boolean) : OnboardingUiEvent
    data object CompleteOnboarding : OnboardingUiEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.NextPage -> {
                _uiState.update { it.copy(currentPage = (it.currentPage + 1).coerceAtMost(4)) }
            }
            is OnboardingUiEvent.PreviousPage -> {
                _uiState.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }
            }
            is OnboardingUiEvent.SetDuration -> {
                val clamped = event.minutes.coerceIn(5, 120)
                _uiState.update { it.copy(selectedDuration = clamped) }
            }
            is OnboardingUiEvent.ToggleAutoLock -> {
                _uiState.update { it.copy(autoLockEnabled = event.enabled) }
            }
            is OnboardingUiEvent.CompleteOnboarding -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            preferencesRepository.setDefaultDuration(state.selectedDuration)
            if (state.autoLockEnabled) {
                preferencesRepository.setAutoLockConfig(
                    AutoLockConfig(enabled = true),
                )
            }
            preferencesRepository.setOnboardingCompleted(true)
        }
    }
}
