package com.wane.app.data.repository.impl

import com.wane.app.data.StreakCalculator
import com.wane.app.data.db.dao.FocusSessionDao
import com.wane.app.data.db.entity.FocusSessionEntity
import com.wane.app.data.repository.SessionRepository
import com.wane.app.shared.CompletionStatus
import com.wane.app.shared.FocusSession
import com.wane.app.shared.StreakInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val streakCalculator: StreakCalculator,
) : SessionRepository {

    override fun observeAllSessions(): Flow<List<FocusSession>> =
        focusSessionDao.getAllSessions()
            .map { list -> list.map(FocusSessionEntity::toShared) }
            .catch { emit(emptyList()) }

    override fun observeRecentSessions(limit: Int): Flow<List<FocusSession>> {
        if (limit <= 0) return flowOf(emptyList())
        return focusSessionDao.getRecentSessions(limit)
            .map { list -> list.map(FocusSessionEntity::toShared) }
            .catch { emit(emptyList()) }
    }

    override fun observeCurrentStreak(): Flow<Int> =
        streakCalculator.observeCurrentStreak()

    override fun observeStreakInfo(): Flow<StreakInfo> =
        streakCalculator.observeStreakInfo()

    override suspend fun recordSession(session: FocusSession): Long = try {
        focusSessionDao.insert(FocusSessionEntity.fromShared(session))
    } catch (_: Exception) {
        0L
    }

    override suspend fun updateSessionEnd(
        sessionId: Long,
        endTime: Long,
        actualDurationMs: Long,
        status: CompletionStatus,
    ) {
        try {
            focusSessionDao.updateSessionEnd(
                sessionId = sessionId,
                endTime = endTime,
                actualDurationMs = actualDurationMs,
                status = status,
            )
        } catch (_: Exception) {
            // no-op
        }
    }

    override suspend fun clearAllSessions() {
        try {
            focusSessionDao.deleteAll()
        } catch (_: Exception) {
            // no-op
        }
    }
}
