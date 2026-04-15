package com.wane.app.service

import com.wane.app.shared.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val sessionState: StateFlow<SessionState>

    val errorEvents: Flow<SessionError>

    suspend fun startSession(
        durationMs: Long,
        themeId: String,
    )

    /**
     * Extends the current session by [additionalMs] wall-clock milliseconds.
     * No-op when not [SessionState.Running]; only the running timer and DB planned duration are updated.
     */
    fun extendSession(additionalMs: Long)

    fun requestEmergencyExit()

    fun confirmSessionComplete()
}

sealed class SessionError {
    data object ForegroundServiceBlocked : SessionError()
}
