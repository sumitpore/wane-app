package com.wane.app.data.db.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.wane.app.data.db.entity.WaterThemeEntity

@Dao
interface WaterThemeDao {

    @Upsert
    suspend fun upsert(theme: WaterThemeEntity)
}
