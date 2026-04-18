package com.wane.app.ui.session

import com.wane.app.animation.TiltSensorManager
import com.wane.app.service.SessionError
import com.wane.app.service.SessionManager
import com.wane.app.shared.SessionState
import com.wane.app.shared.TiltState
import com.wane.app.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val runningSession =
        SessionState.Running(
            sessionId = 1L,
            totalDurationMs = 25 * 60 * 1000L,
            remainingMs = 25 * 60 * 1000L,
            waterLevel = 1f,
        )

    /** Mirrors [SessionViewModel] companion exit phrases for membership checks. */
    private val expectedExitPhrases =
        setOf(
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

    private val extendDurationMs = 5L * 60L * 1000L

    @Test
    fun onEvent_showGraduatedExit_setsVisibleTrue() =
        runTest {
            // Arrange
            val sessionManager = FakeSessionManager(MutableStateFlow(runningSession))
            val tilt = mockTiltSensorManager()
            val viewModel = SessionViewModel(sessionManager, tilt)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            // Act
            viewModel.onEvent(SessionUiEvent.ShowGraduatedExit)
            advanceUntilIdle()

            // Assert
            assertTrue(viewModel.uiState.value.isGraduatedExitVisible)
            job.cancel()
        }

    @Test
    fun onEvent_dismissGraduatedExit_setsVisibleFalse() =
        runTest {
            // Arrange
            val sessionManager = FakeSessionManager(MutableStateFlow(runningSession))
            val tilt = mockTiltSensorManager()
            val viewModel = SessionViewModel(sessionManager, tilt)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            viewModel.onEvent(SessionUiEvent.ShowGraduatedExit)
            advanceUntilIdle()

            // Act
            viewModel.onEvent(SessionUiEvent.DismissGraduatedExit)
            advanceUntilIdle()

            // Assert
            assertFalse(viewModel.uiState.value.isGraduatedExitVisible)
            job.cancel()
        }

    @Test
    fun onEvent_extendSession_callsSessionManagerAndHidesGraduatedExit() =
        runTest {
            // Arrange
            val sessionManager = FakeSessionManager(MutableStateFlow(runningSession))
            val tilt = mockTiltSensorManager()
            val viewModel = SessionViewModel(sessionManager, tilt)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            viewModel.onEvent(SessionUiEvent.ShowGraduatedExit)
            advanceUntilIdle()

            // Act
            viewModel.onEvent(SessionUiEvent.ExtendSession)
            advanceUntilIdle()

            // Assert
            assertEquals(listOf(extendDurationMs), sessionManager.extendSessionCalls)
            assertFalse(viewModel.uiState.value.isGraduatedExitVisible)
            job.cancel()
        }

    @Test
    fun onEvent_proceedToEmergencyExit_hidesGraduatedShowsExitSheetWithPhrase() =
        runTest {
            // Arrange
            val sessionManager = FakeSessionManager(MutableStateFlow(runningSession))
            val tilt = mockTiltSensorManager()
            val viewModel = SessionViewModel(sessionManager, tilt)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            viewModel.onEvent(SessionUiEvent.ShowGraduatedExit)
            advanceUntilIdle()

            // Act
            viewModel.onEvent(SessionUiEvent.ProceedToEmergencyExit)
            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState.value
            assertFalse(state.isGraduatedExitVisible)
            assertTrue(state.isExitSheetVisible)
            assertEquals("", state.exitInput)
            assertTrue(state.exitPhrase in expectedExitPhrases)
            job.cancel()
        }

    @Test
    fun onEvent_confirmEmergencyExit_succeedsWithTrimmedInput() =
        runTest {
            val sessionManager = FakeSessionManager(MutableStateFlow(runningSession))
            val tilt = mockTiltSensorManager()
            val viewModel = SessionViewModel(sessionManager, tilt)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.onEvent(SessionUiEvent.ShowGraduatedExit)
            advanceUntilIdle()
            viewModel.onEvent(SessionUiEvent.ProceedToEmergencyExit)
            advanceUntilIdle()

            val phrase = viewModel.uiState.value.exitPhrase
            viewModel.onEvent(SessionUiEvent.UpdateExitInput("  $phrase  "))
            advanceUntilIdle()

            viewModel.onEvent(SessionUiEvent.ConfirmEmergencyExit)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isExitSheetVisible)
            job.cancel()
        }

    @Test
    fun onEvent_confirmEmergencyExit_rejectedWhenInputDoesNotMatch() =
        runTest {
            val sessionManager = FakeSessionManager(MutableStateFlow(runningSession))
            val tilt = mockTiltSensorManager()
            val viewModel = SessionViewModel(sessionManager, tilt)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.onEvent(SessionUiEvent.ShowGraduatedExit)
            advanceUntilIdle()
            viewModel.onEvent(SessionUiEvent.ProceedToEmergencyExit)
            advanceUntilIdle()

            viewModel.onEvent(SessionUiEvent.UpdateExitInput("wrong phrase"))
            advanceUntilIdle()

            viewModel.onEvent(SessionUiEvent.ConfirmEmergencyExit)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isExitSheetVisible)
            job.cancel()
        }

    private fun mockTiltSensorManager(): TiltSensorManager {
        val tilt = mock<TiltSensorManager>()
        whenever(tilt.tiltFlow).thenReturn(flowOf(TiltState.Unavailable))
        return tilt
    }
}

private class FakeSessionManager(
    override val sessionState: MutableStateFlow<SessionState>,
) : SessionManager {
    override val errorEvents: Flow<SessionError> = emptyFlow()

    val extendSessionCalls = mutableListOf<Long>()

    override suspend fun startSession(
        durationMs: Long,
        themeId: String,
    ) = Unit

    override fun extendSession(additionalMs: Long) {
        extendSessionCalls.add(additionalMs)
    }

    override fun requestEmergencyExit() = Unit

    override fun confirmSessionComplete() = Unit
}
