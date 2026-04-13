package com.wane.app.shared

data class WaterTheme(
    val id: String,
    val displayName: String,
    val isPurchased: Boolean,
    val purchaseToken: String?,
    val purchaseTimestamp: Long?,
)
