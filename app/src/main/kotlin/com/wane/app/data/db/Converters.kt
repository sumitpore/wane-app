package com.wane.app.data.db

import androidx.room.TypeConverter
import com.wane.app.shared.CompletionStatus

class Converters {
    @TypeConverter
    fun fromCompletionStatus(status: CompletionStatus): String = status.name

    @TypeConverter
    fun toCompletionStatus(value: String): CompletionStatus = CompletionStatus.valueOf(value)
}
