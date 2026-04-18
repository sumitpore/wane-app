package com.wane.app.service

import android.app.Application
import android.content.Context
import android.content.Intent
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.shared.AutoLockConfig
import com.wane.app.shared.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AutoLockSchedulerTest {
    private fun mockContext(): Pair<Context, Application> {
        val app = mock<Application>()
        val context = mock<Context>()
        whenever(context.applicationContext).thenReturn(app)
        return Pair(context, app)
    }

    private fun fakePreferencesRepository(autoLockConfig: AutoLockConfig = AutoLockConfig()): PreferencesRepository {
        val repo = mock<PreferencesRepository>()
        whenever(repo.observeAutoLockConfig()).thenReturn(MutableStateFlow(autoLockConfig))
        whenever(repo.observeDefaultDuration()).thenReturn(MutableStateFlow(30))
        whenever(repo.observeOnboardingCompleted()).thenReturn(MutableStateFlow(true))
        return repo
    }

    @Test
    fun `onScreenUnlocked opens app after grace period`() =
        runTest {
            // Arrange
            val config =
                AutoLockConfig(
                    enabled = true,
                    durationMinutes = 45,
                    gracePeriodSeconds = 5,
                )
            val sessionManager = RecordingSessionManager()
            val (context, app) = mockContext()
            val schedulerScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
            val scheduler =
                AutoLockScheduler(
                    preferencesRepository = fakePreferencesRepository(autoLockConfig = config),
                    sessionManager = sessionManager,
                    context = context,
                    scope = schedulerScope,
                )

            // Act
            scheduler.onScreenUnlocked()
            advanceTimeBy(6_000L)
            advanceUntilIdle()

            // Assert -- app is opened, no session started
            verify(app).startActivity(any<Intent>())
            assertTrue(sessionManager.startSessionCalls.isEmpty())
            schedulerScope.cancel()
        }

    @Test
    fun `onScreenUnlocked does not start a session`() =
        runTest {
            // Arrange
            val config =
                AutoLockConfig(
                    enabled = true,
                    durationMinutes = 45,
                    gracePeriodSeconds = 5,
                )
            val sessionManager = RecordingSessionManager()
            val (context, _) = mockContext()
            val schedulerScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
            val scheduler =
                AutoLockScheduler(
                    preferencesRepository = fakePreferencesRepository(autoLockConfig = config),
                    sessionManager = sessionManager,
                    context = context,
                    scope = schedulerScope,
                )

            // Act
            scheduler.onScreenUnlocked()
            advanceTimeBy(6_000L)
            advanceUntilIdle()

            // Assert -- no session auto-started
            assertTrue(sessionManager.startSessionCalls.isEmpty())
            schedulerScope.cancel()
        }

    @Test
    fun `onScreenUnlocked is no-op when config is disabled`() =
        runTest {
            // Arrange
            val config =
                AutoLockConfig(
                    enabled = false,
                    durationMinutes = 45,
                    gracePeriodSeconds = 5,
                )
            val sessionManager = RecordingSessionManager()
            val (context, app) = mockContext()
            val schedulerScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
            val scheduler =
                AutoLockScheduler(
                    preferencesRepository = fakePreferencesRepository(autoLockConfig = config),
                    sessionManager = sessionManager,
                    context = context,
                    scope = schedulerScope,
                )

            // Act
            scheduler.onScreenUnlocked()
            advanceTimeBy(10_000L)
            advanceUntilIdle()

            // Assert
            verify(app, never()).startActivity(any<Intent>())
            assertTrue(sessionManager.startSessionCalls.isEmpty())
            schedulerScope.cancel()
        }

    @Test
    fun `onScreenUnlocked is no-op when session already running`() =
        runTest {
            // Arrange
            val config =
                AutoLockConfig(
                    enabled = true,
                    durationMinutes = 45,
                    gracePeriodSeconds = 5,
                )
            val sessionState =
                MutableStateFlow<SessionState>(
                    SessionState.Running(
                        sessionId = 1L,
                        totalDurationMs = 60_000L,
                        remainingMs = 50_000L,
                        waterLevel = 50_000f / 60_000f,
                    ),
                )
            val sessionManager = RecordingSessionManager(sessionState)
            val (context, app) = mockContext()
            val schedulerScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
            val scheduler =
                AutoLockScheduler(
                    preferencesRepository = fakePreferencesRepository(autoLockConfig = config),
                    sessionManager = sessionManager,
                    context = context,
                    scope = schedulerScope,
                )

            // Act
            scheduler.onScreenUnlocked()
            advanceTimeBy(10_000L)
            advanceUntilIdle()

            // Assert
            verify(app, never()).startActivity(any<Intent>())
            assertTrue(sessionManager.startSessionCalls.isEmpty())
            schedulerScope.cancel()
        }

    @Test
    fun `onScreenLocked cancels pending grace period`() =
        runTest {
            // Arrange
            val config =
                AutoLockConfig(
                    enabled = true,
                    durationMinutes = 30,
                    gracePeriodSeconds = 10,
                )
            val sessionManager = RecordingSessionManager()
            val (context, app) = mockContext()
            val schedulerScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
            val scheduler =
                AutoLockScheduler(
                    preferencesRepository = fakePreferencesRepository(autoLockConfig = config),
                    sessionManager = sessionManager,
                    context = context,
                    scope = schedulerScope,
                )

            // Act
            scheduler.onScreenUnlocked()
            advanceTimeBy(3_000L)
            scheduler.onScreenLocked()
            advanceTimeBy(10_000L)
            advanceUntilIdle()

            // Assert
            verify(app, never()).startActivity(any<Intent>())
            schedulerScope.cancel()
        }

    @Test
    fun `onScreenUnlocked does not open app before grace period elapses`() =
        runTest {
            // Arrange
            val config =
                AutoLockConfig(
                    enabled = true,
                    durationMinutes = 30,
                    gracePeriodSeconds = 10,
                )
            val sessionManager = RecordingSessionManager()
            val (context, app) = mockContext()
            val schedulerScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
            val scheduler =
                AutoLockScheduler(
                    preferencesRepository = fakePreferencesRepository(autoLockConfig = config),
                    sessionManager = sessionManager,
                    context = context,
                    scope = schedulerScope,
                )

            // Act -- advance partway through grace, assert app not opened yet
            scheduler.onScreenUnlocked()
            advanceTimeBy(9_999L)
            verify(app, never()).startActivity(any<Intent>())

            // Advance past grace period
            advanceTimeBy(2L)
            verify(app).startActivity(any<Intent>())
            schedulerScope.cancel()
        }

    @Test
    fun `consecutive onScreenUnlocked calls cancel previous grace and restart`() =
        runTest {
            // Arrange
            val config =
                AutoLockConfig(
                    enabled = true,
                    durationMinutes = 15,
                    gracePeriodSeconds = 10,
                )
            val sessionManager = RecordingSessionManager()
            val (context, app) = mockContext()
            val schedulerScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
            val scheduler =
                AutoLockScheduler(
                    preferencesRepository = fakePreferencesRepository(autoLockConfig = config),
                    sessionManager = sessionManager,
                    context = context,
                    scope = schedulerScope,
                )

            // Act -- first unlock, then a second unlock 5s later restarts the grace period
            scheduler.onScreenUnlocked()
            advanceTimeBy(5_000L)
            scheduler.onScreenUnlocked()
            advanceTimeBy(5_000L)

            // Assert -- second grace period hasn't finished
            verify(app, never()).startActivity(any<Intent>())

            advanceTimeBy(6_000L)

            // Now the second grace period has elapsed, app should open
            verify(app).startActivity(any<Intent>())
            schedulerScope.cancel()
        }
}

private class RecordingSessionManager(
    override val sessionState: MutableStateFlow<SessionState> = MutableStateFlow(SessionState.Idle),
) : SessionManager {
    override val errorEvents: Flow<SessionError> = emptyFlow()

    val startSessionCalls = mutableListOf<Pair<Long, String>>()

    override suspend fun startSession(
        durationMs: Long,
        themeId: String,
    ) {
        startSessionCalls.add(Pair(durationMs, themeId))
    }

    override fun extendSession(additionalMs: Long) = Unit

    override fun requestEmergencyExit() = Unit

    override fun confirmSessionComplete() = Unit
}
