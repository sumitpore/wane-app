package com.wane.app.data.repository

import com.wane.app.shared.CompletionStatus
import com.wane.app.shared.FocusSession
import com.wane.app.shared.StreakInfo
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeStreakInfo(): Flow<StreakInfo>

    suspend fun recordSession(session: FocusSession): Long

    suspend fun updateSessionEnd(
        sessionId: Long,
        endTime: Long,
        actualDurationMs: Long,
        status: CompletionStatus,
    )

    suspend fun updateSessionPlannedDuration(
        sessionId: Long,
        plannedDurationMs: Long,
    )

    suspend fun clearAllSessions()
}
