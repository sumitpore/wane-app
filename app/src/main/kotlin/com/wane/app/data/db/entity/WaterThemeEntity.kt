package com.wane.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wane.app.shared.WaterTheme

@Entity(tableName = "water_themes")
data class WaterThemeEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val isPurchased: Boolean,
    val purchaseToken: String?,
    val purchaseTimestamp: Long?,
) {

    fun toShared(): WaterTheme = WaterTheme(
        id = id,
        displayName = displayName,
        isPurchased = isPurchased,
        purchaseToken = purchaseToken,
        purchaseTimestamp = purchaseTimestamp,
    )

    companion object {
        fun fromShared(theme: WaterTheme): WaterThemeEntity = WaterThemeEntity(
            id = theme.id,
            displayName = theme.displayName,
            isPurchased = theme.isPurchased,
            purchaseToken = theme.purchaseToken,
            purchaseTimestamp = theme.purchaseTimestamp,
        )
    }
}
