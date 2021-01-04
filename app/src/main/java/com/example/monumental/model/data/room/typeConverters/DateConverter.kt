package com.example.monumental.model.data.room.typeConverters

import androidx.room.TypeConverter
import java.util.*

class DateConverter {

    /**
     * Converts a timestamp to a Date
     *
     * @param dateLong timestamp
     * @return Date
     */
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    /**
     * Converts a Date to a timestamp
     *
     * @param date Date
     * @return Long timestamp
     */
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
}
