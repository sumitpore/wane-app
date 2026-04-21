package com.unclutteredapps.wane.data.repository.impl

import com.unclutteredapps.wane.data.StreakCalculator
import com.unclutteredapps.wane.data.db.dao.FocusSessionDao
import com.unclutteredapps.wane.data.db.entity.FocusSessionEntity
import com.unclutteredapps.wane.data.repository.SessionRepository
import com.unclutteredapps.wane.shared.CompletionStatus
import com.unclutteredapps.wane.shared.FocusSession
import com.unclutteredapps.wane.shared.StreakInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl
    @Inject
    constructor(
        private val focusSessionDao: FocusSessionDao,
        private val streakCalculator: StreakCalculator,
    ) : SessionRepository {
        override fun observeStreakInfo(): Flow<StreakInfo> = streakCalculator.observeStreakInfo()

        override suspend fun recordSession(session: FocusSession): Long =
            try {
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

        override suspend fun updateSessionPlannedDuration(
            sessionId: Long,
            plannedDurationMs: Long,
        ) {
            try {
                focusSessionDao.updatePlannedDuration(sessionId, plannedDurationMs)
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
