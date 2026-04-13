package com.wane.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wane.app.data.db.entity.FocusSessionEntity
import com.wane.app.shared.CompletionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<FocusSessionEntity>>

    @Query(
        """
        SELECT * FROM focus_sessions
        WHERE completionStatus = 'COMPLETED'
        ORDER BY startTime DESC
        """,
    )
    fun observeCompletedSessions(): Flow<List<FocusSessionEntity>>

    @Insert
    suspend fun insert(session: FocusSessionEntity): Long

    @Query(
        """
        UPDATE focus_sessions
        SET endTime = :endTime,
            actualDurationMs = :actualDurationMs,
            completionStatus = :status
        WHERE id = :sessionId
        """,
    )
    suspend fun updateSessionEnd(
        sessionId: Long,
        endTime: Long,
        actualDurationMs: Long,
        status: CompletionStatus,
    )

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAll()

    /**
     * Longest run of consecutive local calendar days with at least one COMPLETED session.
     * Uses a window function over distinct completion days (islands pattern).
     */
    @Query(
        """
        WITH distinct_days AS (
            SELECT DISTINCT date(startTime / 1000, 'unixepoch', 'localtime') AS day
            FROM focus_sessions
            WHERE completionStatus = 'COMPLETED'
        ),
        numbered AS (
            SELECT
                day,
                julianday(day) - ROW_NUMBER() OVER (ORDER BY julianday(day)) AS grp
            FROM distinct_days
        ),
        streaks AS (
            SELECT COUNT(*) AS streak_len FROM numbered GROUP BY grp
        )
        SELECT COALESCE(MAX(streak_len), 0) FROM streaks
        """,
    )
    fun observeLongestCompletionStreakDays(): Flow<Int>

    @Query("SELECT COUNT(*) FROM focus_sessions")
    fun observeTotalSessionCount(): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(actualDurationMs), 0)
        FROM focus_sessions
        WHERE completionStatus = 'COMPLETED'
        """,
    )
    fun observeTotalCompletedDurationMs(): Flow<Long>
}
