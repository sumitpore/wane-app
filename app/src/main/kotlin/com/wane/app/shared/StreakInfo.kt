package com.wane.app.shared

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalSessions: Int,
    val totalMinutes: Long,
)
