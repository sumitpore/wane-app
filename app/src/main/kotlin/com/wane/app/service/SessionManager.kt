package com.wane.app.service

import com.wane.app.shared.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionManager {

    val sessionState: StateFlow<SessionState>

    val errorEvents: Flow<SessionError>

    suspend fun startSession(durationMs: Long, themeId: String)

    fun requestEmergencyExit()

    fun confirmSessionComplete()
}

sealed class SessionError {
    data object ForegroundServiceBlocked : SessionError()
}
