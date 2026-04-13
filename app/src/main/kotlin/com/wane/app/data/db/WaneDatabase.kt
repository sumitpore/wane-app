package com.wane.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wane.app.data.db.dao.FocusSessionDao
import com.wane.app.data.db.dao.WaterThemeDao
import com.wane.app.data.db.entity.FocusSessionEntity
import com.wane.app.data.db.entity.WaterThemeEntity

@Database(
    entities = [
        FocusSessionEntity::class,
        WaterThemeEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class WaneDatabase : RoomDatabase() {

    abstract fun focusSessionDao(): FocusSessionDao

    abstract fun waterThemeDao(): WaterThemeDao
}
