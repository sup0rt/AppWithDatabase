package com.example.appwithdatabase.data.database

import android.content.Context
import android.graphics.Color
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appwithdatabase.data.dao.CategoryDao
import com.example.appwithdatabase.data.dao.TaskDao
import com.example.appwithdatabase.data.entities.CategoryEntity
import com.example.appwithdatabase.data.entities.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [TaskEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taskmaster_database.db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Используем глобальный scope вместо создания нового
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prepopulateData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        // Вынесем prepopulateData в companion object
        private suspend fun prepopulateData(database: AppDatabase) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Работа", color = Color.BLUE, createdAt = Date()),
                CategoryEntity(name = "Личное", color = Color.GREEN, createdAt = Date()),
                CategoryEntity(name = "Здоровье", color = Color.RED, createdAt = Date()),
                CategoryEntity(name = "Обучение", color = Color.YELLOW, createdAt = Date())
            )

            defaultCategories.forEach { database.categoryDao().insertCategory(it) }
        }
    }
}