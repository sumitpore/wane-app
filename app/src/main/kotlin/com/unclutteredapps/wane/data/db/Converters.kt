package com.unclutteredapps.wane.data.db

import androidx.room.TypeConverter
import com.unclutteredapps.wane.shared.CompletionStatus

class Converters {
    @TypeConverter
    fun fromCompletionStatus(status: CompletionStatus): String = status.name

    @TypeConverter
    fun toCompletionStatus(value: String): CompletionStatus = CompletionStatus.valueOf(value)
}
