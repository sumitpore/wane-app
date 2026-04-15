package com.wane.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wane.app.data.db.dao.FocusSessionDao
import com.wane.app.data.db.entity.FocusSessionEntity

@Database(
    entities = [
        FocusSessionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class WaneDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
}
