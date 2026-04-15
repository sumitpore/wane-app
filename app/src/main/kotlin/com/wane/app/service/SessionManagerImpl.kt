package com.wane.app.service

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.wane.app.data.repository.SessionRepository
import com.wane.app.shared.CompletionStatus
import com.wane.app.shared.FocusSession
import com.wane.app.shared.SessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Singleton
class SessionManagerImpl
    @Inject
    constructor(
        private val sessionRepository: SessionRepository,
        private val repeatedCallerTracker: RepeatedCallerTracker,
        @ApplicationContext private val context: Context,
        @ApplicationScope private val scope: CoroutineScope,
    ) : SessionManager {
        private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
        override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

        private val errorChannel = Channel<SessionError>(Channel.BUFFERED)
        override val errorEvents: Flow<SessionError> = errorChannel.receiveAsFlow()

        private var timerJob: Job? = null
        private var sessionStartWallClockMs: Long = 0L

        /** Monotonic start anchor for the active session timer ([SystemClock.elapsedRealtime]). */
        private var sessionStartElapsedRealtime: Long = 0L

        /** Total planned duration for the active session; may increase when [extendSession] runs. */
        private val sessionTotalDurationMs = AtomicLong(0L)

        override suspend fun startSession(
            durationMs: Long,
            themeId: String,
        ) {
            if (_sessionState.value is SessionState.Running) {
                Log.w(TAG, "startSession ignored: already Running")
                return
            }
            require(durationMs > 0) { "durationMs must be positive" }

            val session =
                FocusSession(
                    id = 0L,
                    startTime = System.currentTimeMillis(),
                    endTime = 0L,
                    plannedDurationMs = durationMs,
                    actualDurationMs = 0L,
                    completionStatus = CompletionStatus.COMPLETED,
                    themeId = themeId,
                )
            val sessionId = sessionRepository.recordSession(session)
            sessionStartWallClockMs = session.startTime

            sessionStartElapsedRealtime = SystemClock.elapsedRealtime()
            sessionTotalDurationMs.set(durationMs)

            _sessionState.value =
                SessionState.Running(
                    sessionId = sessionId,
                    totalDurationMs = durationMs,
                    remainingMs = durationMs,
                    waterLevel = 1f,
                )

            try {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, WaneSessionService::class.java).apply {
                        action = WaneSessionService.ACTION_START
                    },
                )
            } catch (e: ForegroundServiceStartNotAllowedException) {
                Log.e(TAG, "Foreground service start not allowed", e)
                errorChannel.trySend(SessionError.ForegroundServiceBlocked)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service", e)
            }

            timerJob?.cancel()
            timerJob =
                scope.launch {
                    try {
                        val startElapsed = sessionStartElapsedRealtime
                        while (isActive) {
                            val elapsed = SystemClock.elapsedRealtime() - startElapsed
                            val total = sessionTotalDurationMs.get()
                            val remaining = (total - elapsed).coerceAtLeast(0L)
                            val water =
                                if (total > 0L) {
                                    remaining.toFloat() / total.toFloat()
                                } else {
                                    0f
                                }
                            _sessionState.value =
                                SessionState.Running(
                                    sessionId = sessionId,
                                    totalDurationMs = total,
                                    remainingMs = remaining,
                                    waterLevel = water,
                                )
                            if (remaining <= 0L) break
                            delay(TIMER_TICK_MS)
                        }
                        if (!isActive) return@launch

                        val finalTotal = sessionTotalDurationMs.get()
                        val endTime = System.currentTimeMillis()
                        sessionRepository.updateSessionEnd(
                            sessionId = sessionId,
                            endTime = endTime,
                            actualDurationMs = finalTotal,
                            status = CompletionStatus.COMPLETED,
                        )
                        _sessionState.value =
                            SessionState.Completing(
                                sessionId = sessionId,
                                totalDurationMs = finalTotal,
                                actualDurationMs = finalTotal,
                            )
                        sessionTotalDurationMs.set(0L)
                        sessionStartElapsedRealtime = 0L
                        stopSessionService()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "Session timer failed", e)
                    }
                }
        }

        override fun extendSession(additionalMs: Long) {
            try {
                val running =
                    _sessionState.value as? SessionState.Running ?: run {
                        Log.w(TAG, "extendSession ignored: not Running")
                        return
                    }
                if (additionalMs <= 0L) {
                    Log.w(TAG, "extendSession ignored: additionalMs must be positive")
                    return
                }
                val newTotal = sessionTotalDurationMs.addAndGet(additionalMs)
                val elapsed = SystemClock.elapsedRealtime() - sessionStartElapsedRealtime
                val remaining = (newTotal - elapsed).coerceAtLeast(0L)
                val water =
                    if (newTotal > 0L) {
                        remaining.toFloat() / newTotal.toFloat()
                    } else {
                        0f
                    }
                _sessionState.value =
                    SessionState.Running(
                        sessionId = running.sessionId,
                        totalDurationMs = newTotal,
                        remainingMs = remaining,
                        waterLevel = water,
                    )
                val sessionId = running.sessionId
                scope.launch {
                    try {
                        sessionRepository.updateSessionPlannedDuration(sessionId, newTotal)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "extendSession: failed to persist planned duration", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "extendSession failed", e)
            }
        }

        override fun requestEmergencyExit() {
            val running = _sessionState.value as? SessionState.Running ?: return
            timerJob?.cancel()
            timerJob = null
            sessionTotalDurationMs.set(0L)
            sessionStartElapsedRealtime = 0L
            val sessionId = running.sessionId
            scope.launch {
                try {
                    val endTime = System.currentTimeMillis()
                    val actual = (endTime - sessionStartWallClockMs).coerceAtLeast(0L)
                    sessionRepository.updateSessionEnd(
                        sessionId = sessionId,
                        endTime = endTime,
                        actualDurationMs = actual,
                        status = CompletionStatus.EARLY_EXIT,
                    )
                    _sessionState.value = SessionState.EmergencyExit
                    stopSessionService()
                    delay(EMERGENCY_EXIT_UI_DELAY_MS)
                    _sessionState.value = SessionState.Idle
                    repeatedCallerTracker.reset()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "requestEmergencyExit failed", e)
                }
            }
        }

        override fun confirmSessionComplete() {
            if (_sessionState.value !is SessionState.Completing) return
            scope.launch {
                try {
                    _sessionState.value = SessionState.Idle
                    stopSessionService()
                    repeatedCallerTracker.reset()
                } catch (e: Exception) {
                    Log.e(TAG, "confirmSessionComplete failed", e)
                }
            }
        }

        private fun stopSessionService() {
            try {
                context.startService(
                    Intent(context, WaneSessionService::class.java).apply {
                        action = WaneSessionService.ACTION_STOP
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "stopSessionService failed", e)
            }
        }

        companion object {
            private const val TAG = "SessionManagerImpl"
            private const val TIMER_TICK_MS = 50L
            private const val EMERGENCY_EXIT_UI_DELAY_MS = 600L
        }
    }
