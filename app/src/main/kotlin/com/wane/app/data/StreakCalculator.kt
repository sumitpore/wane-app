package com.wane.app.data

import com.wane.app.data.db.dao.FocusSessionDao
import com.wane.app.data.db.entity.FocusSessionEntity
import com.wane.app.shared.StreakInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class StreakCalculator
    @Inject
    constructor(
        private val focusSessionDao: FocusSessionDao,
    ) {
        fun observeStreakInfo(): Flow<StreakInfo> =
            combine(
                focusSessionDao.observeCompletedSessions(),
                focusSessionDao.observeLongestCompletionStreakDays(),
                focusSessionDao.observeTotalSessionCount(),
                focusSessionDao.observeTotalCompletedDurationMs(),
            ) { completed, longestSql, totalCount, totalDurationMs ->
                val current = computeCurrentStreak(completed)
                val longest = maxOf(longestSql, current)
                StreakInfo(
                    currentStreak = current,
                    longestStreak = longest,
                    totalSessions = totalCount,
                    totalMinutes = totalDurationMs / 60_000L,
                )
            }.catch {
                emit(StreakInfo(0, 0, 0, 0L))
            }

        private fun computeCurrentStreak(completedSessions: List<FocusSessionEntity>): Int {
            if (completedSessions.isEmpty()) return 0
            val zone = ZoneId.systemDefault()
            val completedDays =
                completedSessions
                    .map { session ->
                        Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
                    }.toSet()
            val today = LocalDate.now(zone)
            val yesterday = today.minusDays(1)
            val anchor =
                when {
                    completedDays.contains(today) -> today
                    completedDays.contains(yesterday) -> yesterday
                    else -> return 0
                }
            var count = 0
            var d = anchor
            while (completedDays.contains(d)) {
                count++
                d = d.minusDays(1)
            }
            return count
        }
    }
