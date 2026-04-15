package com.wane.app.data

import com.wane.app.data.db.dao.FocusSessionDao
import com.wane.app.data.db.entity.FocusSessionEntity
import com.wane.app.shared.CompletionStatus
import com.wane.app.shared.StreakInfo
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StreakCalculatorTest {

    private lateinit var fakeDao: FakeFocusSessionDao
    private lateinit var calculator: StreakCalculator

    @Before
    fun setUp() {
        fakeDao = FakeFocusSessionDao()
        calculator = StreakCalculator(fakeDao)
    }

    @Test
    fun `empty sessions yields streak of 0`() = runTest {
        fakeDao.setCompletedSessions(emptyList())
        assertEquals(0, calculator.observeCurrentStreak().first())
    }

    @Test
    fun `single session today yields streak of 1`() = runTest {
        fakeDao.setCompletedSessions(listOf(sessionOnDate(today())))
        assertEquals(1, calculator.observeCurrentStreak().first())
    }

    @Test
    fun `sessions today and yesterday yields streak of 2`() = runTest {
        fakeDao.setCompletedSessions(
            listOf(sessionOnDate(today()), sessionOnDate(today().minusDays(1))),
        )
        assertEquals(2, calculator.observeCurrentStreak().first())
    }

    @Test
    fun `gap in sessions returns streak of 1`() = runTest {
        fakeDao.setCompletedSessions(
            listOf(sessionOnDate(today()), sessionOnDate(today().minusDays(3))),
        )
        assertEquals(1, calculator.observeCurrentStreak().first())
    }

    @Test
    fun `session yesterday only yields streak of 1`() = runTest {
        fakeDao.setCompletedSessions(listOf(sessionOnDate(today().minusDays(1))))
        assertEquals(1, calculator.observeCurrentStreak().first())
    }

    @Test
    fun `session two days ago only yields streak of 0`() = runTest {
        fakeDao.setCompletedSessions(listOf(sessionOnDate(today().minusDays(2))))
        assertEquals(0, calculator.observeCurrentStreak().first())
    }

    @Test
    fun `multiple sessions on same day count as one streak day`() = runTest {
        fakeDao.setCompletedSessions(
            listOf(
                sessionOnDate(today(), hourOfDay = 9),
                sessionOnDate(today(), hourOfDay = 14),
                sessionOnDate(today(), hourOfDay = 20),
            ),
        )
        assertEquals(1, calculator.observeCurrentStreak().first())
    }

    @Test
    fun `consecutive five day streak`() = runTest {
        val sessions = (0L..4L).map { daysAgo -> sessionOnDate(today().minusDays(daysAgo)) }
        fakeDao.setCompletedSessions(sessions)
        assertEquals(5, calculator.observeCurrentStreak().first())
    }

    @Test
    fun `observeStreakInfo returns correct aggregate data`() = runTest {
        val sessions = listOf(
            sessionOnDate(today(), durationMs = 60_000L),
            sessionOnDate(today().minusDays(1), durationMs = 120_000L),
        )
        fakeDao.setCompletedSessions(sessions)
        fakeDao.longestStreakDays = 3
        fakeDao.totalSessionCount = 10
        fakeDao.totalCompletedDurationMs = 600_000L

        val info: StreakInfo = calculator.observeStreakInfo().first()

        assertEquals(2, info.currentStreak)
        assertEquals(3, info.longestStreak)
        assertEquals(10, info.totalSessions)
        assertEquals(10L, info.totalMinutes)
    }

    @Test
    fun `observeStreakInfo longestStreak is max of sql and current`() = runTest {
        val sessions = (0L..6L).map { daysAgo -> sessionOnDate(today().minusDays(daysAgo)) }
        fakeDao.setCompletedSessions(sessions)
        fakeDao.longestStreakDays = 3
        fakeDao.totalSessionCount = 7
        fakeDao.totalCompletedDurationMs = 0L

        val info = calculator.observeStreakInfo().first()

        assertEquals(7, info.currentStreak)
        assertEquals(7, info.longestStreak)
    }

    private fun today(): LocalDate = LocalDate.now(ZoneId.systemDefault())

    private fun sessionOnDate(
        date: LocalDate,
        hourOfDay: Int = 12,
        durationMs: Long = 25 * 60_000L,
    ): FocusSessionEntity {
        val startMillis = date.atTime(hourOfDay, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return FocusSessionEntity(
            id = 0,
            startTime = startMillis,
            endTime = startMillis + durationMs,
            plannedDurationMs = durationMs,
            actualDurationMs = durationMs,
            completionStatus = CompletionStatus.COMPLETED,
            themeId = "default",
        )
    }
}

private class FakeFocusSessionDao : FocusSessionDao {

    private val completedSessions = MutableStateFlow<List<FocusSessionEntity>>(emptyList())
    var longestStreakDays: Int = 0
    var totalSessionCount: Int = 0
    var totalCompletedDurationMs: Long = 0L

    fun setCompletedSessions(sessions: List<FocusSessionEntity>) {
        completedSessions.value = sessions
    }

    override fun observeCompletedSessions(): Flow<List<FocusSessionEntity>> = completedSessions

    override suspend fun insert(session: FocusSessionEntity): Long = 1L

    override suspend fun updateSessionEnd(
        sessionId: Long,
        endTime: Long,
        actualDurationMs: Long,
        status: CompletionStatus,
    ) = Unit

    override suspend fun deleteAll() = Unit

    override fun observeLongestCompletionStreakDays(): Flow<Int> =
        MutableStateFlow(longestStreakDays)

    override fun observeTotalSessionCount(): Flow<Int> =
        MutableStateFlow(totalSessionCount)

    override fun observeTotalCompletedDurationMs(): Flow<Long> =
        MutableStateFlow(totalCompletedDurationMs)
}
