package com.wane.app.service

import android.content.Context
import android.os.SystemClock
import com.wane.app.shared.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.concurrent.atomic.AtomicLong

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerImplTest {
    @Test
    fun extendSession_whenRunning_increasesTotalDurationAndRemainingAndWaterLevel() =
        runTest {
            // Arrange
            val repo = FakeSessionRepository()
            val impl =
                SessionManagerImpl(
                    repo,
                    RepeatedCallerTracker(),
                    mock<Context>(),
                    this as CoroutineScope,
                )
            seedRunningSession(
                impl = impl,
                sessionId = 1L,
                totalDurationMs = 60_000L,
                remainingMs = 50_000L,
            )

            // Act
            impl.extendSession(30_000L)
            advanceUntilIdle()

            // Assert
            val running = impl.sessionState.value as SessionState.Running
            assertEquals(90_000L, running.totalDurationMs)
            assertEquals(80_000L, running.remainingMs)
            assertEquals(80_000f / 90_000f, running.waterLevel, 1e-5f)
            assertEquals(listOf(1L to 90_000L), repo.plannedDurationUpdates)
        }

    @Test
    fun extendSession_whenRunning_updatesRunningStateSnapshot() =
        runTest {
            // Arrange
            val repo = FakeSessionRepository()
            val impl =
                SessionManagerImpl(
                    repo,
                    RepeatedCallerTracker(),
                    mock<Context>(),
                    this as CoroutineScope,
                )
            seedRunningSession(
                impl = impl,
                sessionId = 42L,
                totalDurationMs = 100_000L,
                remainingMs = 100_000L,
            )

            // Act
            impl.extendSession(25_000L)
            advanceUntilIdle()

            // Assert
            val running = impl.sessionState.value as SessionState.Running
            assertEquals(42L, running.sessionId)
            assertEquals(125_000L, running.totalDurationMs)
            assertEquals(125_000L, running.remainingMs)
            assertEquals(1f, running.waterLevel, 1e-5f)
        }

    @Test
    fun extendSession_whenIdle_isNoOp() =
        runTest {
            // Arrange
            val repo = FakeSessionRepository()
            val impl =
                SessionManagerImpl(
                    repo,
                    RepeatedCallerTracker(),
                    mock<Context>(),
                    this as CoroutineScope,
                )

            // Act
            impl.extendSession(60_000L)
            advanceUntilIdle()

            // Assert
            assertEquals(SessionState.Idle, impl.sessionState.value)
            assertTrue(repo.plannedDurationUpdates.isEmpty())
        }

    @Test
    fun extendSession_whenCompleting_isNoOp() =
        runTest {
            // Arrange
            val repo = FakeSessionRepository()
            val impl =
                SessionManagerImpl(
                    repo,
                    RepeatedCallerTracker(),
                    mock<Context>(),
                    this as CoroutineScope,
                )
            val stateFlow = sessionStateFlow(impl)
            stateFlow.value =
                SessionState.Completing(
                    sessionId = 1L,
                    totalDurationMs = 60_000L,
                    actualDurationMs = 60_000L,
                )

            // Act
            impl.extendSession(30_000L)
            advanceUntilIdle()

            // Assert
            assertTrue(impl.sessionState.value is SessionState.Completing)
            assertTrue(repo.plannedDurationUpdates.isEmpty())
        }

    @Test
    fun extendSession_whenAdditionalMsZero_isNoOp() =
        runTest {
            // Arrange
            val repo = FakeSessionRepository()
            val impl =
                SessionManagerImpl(
                    repo,
                    RepeatedCallerTracker(),
                    mock<Context>(),
                    this as CoroutineScope,
                )
            seedRunningSession(
                impl = impl,
                sessionId = 1L,
                totalDurationMs = 60_000L,
                remainingMs = 50_000L,
            )
            val totalBefore = sessionTotalDurationMs(impl).get()

            // Act
            impl.extendSession(0L)
            advanceUntilIdle()

            // Assert
            assertEquals(totalBefore, sessionTotalDurationMs(impl).get())
            val running = impl.sessionState.value as SessionState.Running
            assertEquals(60_000L, running.totalDurationMs)
            assertTrue(repo.plannedDurationUpdates.isEmpty())
        }

    @Test
    fun extendSession_whenAdditionalMsNegative_isNoOp() =
        runTest {
            // Arrange
            val repo = FakeSessionRepository()
            val impl =
                SessionManagerImpl(
                    repo,
                    RepeatedCallerTracker(),
                    mock<Context>(),
                    this as CoroutineScope,
                )
            seedRunningSession(
                impl = impl,
                sessionId = 1L,
                totalDurationMs = 60_000L,
                remainingMs = 50_000L,
            )
            val totalBefore = sessionTotalDurationMs(impl).get()

            // Act
            impl.extendSession(-1L)
            advanceUntilIdle()

            // Assert
            assertEquals(totalBefore, sessionTotalDurationMs(impl).get())
            assertTrue(repo.plannedDurationUpdates.isEmpty())
        }
}

private fun sessionStateFlow(impl: SessionManagerImpl): MutableStateFlow<SessionState> {
    val m =
        SessionManagerImpl::class.java.getMethod(
            "access\$get_sessionState\$p",
            SessionManagerImpl::class.java,
        )
    @Suppress("UNCHECKED_CAST")
    return m.invoke(null, impl) as MutableStateFlow<SessionState>
}

private fun sessionTotalDurationMs(impl: SessionManagerImpl): AtomicLong {
    val m =
        SessionManagerImpl::class.java.getMethod(
            "access\$getSessionTotalDurationMs\$p",
            SessionManagerImpl::class.java,
        )
    return m.invoke(null, impl) as AtomicLong
}

private fun seedRunningSession(
    impl: SessionManagerImpl,
    sessionId: Long,
    totalDurationMs: Long,
    remainingMs: Long,
) {
    val elapsed = totalDurationMs - remainingMs
    val anchorElapsed = SystemClock.elapsedRealtime() - elapsed
    val setElapsed =
        SessionManagerImpl::class.java.getMethod(
            "access\$setSessionStartElapsedRealtime\$p",
            SessionManagerImpl::class.java,
            Long::class.javaPrimitiveType,
        )
    setElapsed.invoke(null, impl, anchorElapsed)

    sessionTotalDurationMs(impl).set(totalDurationMs)

    val water =
        if (totalDurationMs > 0) {
            remainingMs.toFloat() / totalDurationMs.toFloat()
        } else {
            0f
        }
    sessionStateFlow(impl).value =
        SessionState.Running(
            sessionId = sessionId,
            totalDurationMs = totalDurationMs,
            remainingMs = remainingMs,
            waterLevel = water,
        )
}
