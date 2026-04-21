package com.unclutteredapps.wane.data.repository

import com.unclutteredapps.wane.shared.CompletionStatus
import com.unclutteredapps.wane.shared.FocusSession
import com.unclutteredapps.wane.shared.StreakInfo
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
