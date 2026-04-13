package com.wane.app.shared

sealed class SessionState {

    data object Idle : SessionState()

    data class Running(
        val sessionId: Long,
        val totalDurationMs: Long,
        val remainingMs: Long,
        val waterLevel: Float,
    ) : SessionState()

    data class Completing(
        val sessionId: Long,
        val totalDurationMs: Long,
        val actualDurationMs: Long,
    ) : SessionState()

    data object EmergencyExit : SessionState()
}
