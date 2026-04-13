package com.wane.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wane.app.shared.CompletionStatus
import com.wane.app.shared.FocusSession

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val plannedDurationMs: Long,
    val actualDurationMs: Long,
    val completionStatus: CompletionStatus,
    val themeId: String,
) {

    fun toShared(): FocusSession = FocusSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        plannedDurationMs = plannedDurationMs,
        actualDurationMs = actualDurationMs,
        completionStatus = completionStatus,
        themeId = themeId,
    )

    companion object {
        fun fromShared(session: FocusSession): FocusSessionEntity = FocusSessionEntity(
            id = session.id,
            startTime = session.startTime,
            endTime = session.endTime,
            plannedDurationMs = session.plannedDurationMs,
            actualDurationMs = session.actualDurationMs,
            completionStatus = session.completionStatus,
            themeId = session.themeId,
        )
    }
}
