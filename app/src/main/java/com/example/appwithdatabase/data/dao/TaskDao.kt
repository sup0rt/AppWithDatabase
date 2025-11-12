package com.example.appwithdatabase.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.appwithdatabase.data.entities.TaskEntity
import com.example.appwithdatabase.data.entities.relations.TaskWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE task_id=:taskId")
    suspend fun getTasksById(taskId: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category_id=:categoryId")
    fun getTasksByCategory(categoryId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed=:isCompleted")
    fun getTasksByComletion(isCompleted: Boolean): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE priority=:priority")
    fun getTasksByPriority(priority: TaskEntity.Priority): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE title LIKE '%'||:query" +
            "||'%' OR description LIKE '%'||:query||'%'")
    fun searchTasks(query: String): Flow<List<TaskEntity>>


    @Query("UPDATE tasks SET is_completed=:isCompleted WHERE task_id=:taskId")
    suspend fun updatetTaskCompletion(taskId: Long, isCompleted: Boolean)

    @Transaction
    @Query("SELECT * FROM tasks WHERE task_id=:taskId")
    suspend fun getTaskWithCategory(taskId: Long): TaskWithCategory?

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1")
    suspend fun getCompletedTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTotalTaskCount(): Int
}