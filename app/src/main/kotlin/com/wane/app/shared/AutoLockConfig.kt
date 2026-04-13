package com.wane.app.shared

data class AutoLockConfig(
    val enabled: Boolean = false,
    val durationMinutes: Int = 30,
    val gracePeriodSeconds: Int = 10,
    val skipStartHour: Int? = null,
    val skipStartMinute: Int? = null,
    val skipEndHour: Int? = null,
    val skipEndMinute: Int? = null,
    val skipWhileCharging: Boolean = false,
)
