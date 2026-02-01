package com.hdaf.eduapp.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room TypeConverter for Date objects.
 */
class DateConverter {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
