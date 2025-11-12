package com.example.appwithdatabase.data.database

import androidx.room.TypeConverter
import com.example.appwithdatabase.data.entities.TaskEntity
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date?{
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long?{
        return date?.time
    }

    @TypeConverter
    fun fromPriority(priority: TaskEntity.Priority): String{
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): TaskEntity.Priority {
        return TaskEntity.Priority.valueOf(priority)
    }
}