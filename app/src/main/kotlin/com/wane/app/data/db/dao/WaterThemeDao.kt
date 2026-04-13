package com.wane.app.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wane.app.data.db.entity.WaterThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterThemeDao {

    @Query("SELECT * FROM water_themes ORDER BY displayName ASC")
    fun getAllThemes(): Flow<List<WaterThemeEntity>>

    @Query("SELECT * FROM water_themes WHERE isPurchased = 1 ORDER BY displayName ASC")
    fun getPurchasedThemes(): Flow<List<WaterThemeEntity>>

    @Query("SELECT * FROM water_themes WHERE id = :id LIMIT 1")
    suspend fun getThemeById(id: String): WaterThemeEntity?

    @Upsert
    suspend fun upsert(theme: WaterThemeEntity)

    @Query(
        """
        UPDATE water_themes
        SET isPurchased = 1,
            purchaseToken = :purchaseToken,
            purchaseTimestamp = :purchaseTimestamp
        WHERE id = :themeId
        """,
    )
    suspend fun updatePurchaseStatus(
        themeId: String,
        purchaseToken: String,
        purchaseTimestamp: Long,
    ): Int
}
