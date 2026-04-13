package com.wane.app.shared

data class FocusSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val plannedDurationMs: Long,
    val actualDurationMs: Long,
    val completionStatus: CompletionStatus,
    val themeId: String,
)
