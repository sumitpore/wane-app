package com.wane.app.service

import com.wane.app.data.repository.SessionRepository
import com.wane.app.shared.CompletionStatus
import com.wane.app.shared.FocusSession
import com.wane.app.shared.StreakInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeSessionRepository : SessionRepository {
    private val streakInfo = MutableStateFlow(StreakInfo(0, 0, 0, 0L))

    var nextSessionId: Long = 1L

    val recordedSessions = mutableListOf<FocusSession>()
    val plannedDurationUpdates = mutableListOf<Pair<Long, Long>>()

    override fun observeStreakInfo(): Flow<StreakInfo> = streakInfo

    override suspend fun recordSession(session: FocusSession): Long {
        recordedSessions.add(session)
        return nextSessionId
    }

    override suspend fun updateSessionEnd(
        sessionId: Long,
        endTime: Long,
        actualDurationMs: Long,
        status: CompletionStatus,
    ) = Unit

    override suspend fun updateSessionPlannedDuration(
        sessionId: Long,
        plannedDurationMs: Long,
    ) {
        plannedDurationUpdates.add(sessionId to plannedDurationMs)
    }

    override suspend fun clearAllSessions() = Unit
}
