package com.example.appwithdatabase.domain.repository

import com.example.appwithdatabase.data.entities.CategoryEntity
import com.example.appwithdatabase.data.entities.TaskEntity
import com.example.appwithdatabase.domain.models.TaskStatistics
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<TaskEntity>>
    fun getTasksByCategory(categoryId: Long): Flow<List<TaskEntity>>
    suspend fun createTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean)
    fun getTaskStatistics(): TaskStatistics

    // Методы для работы с категориями
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategoryById(categoryId: Long): CategoryEntity?
    suspend fun createCategory(category: CategoryEntity): Long
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(categoryId: Long)
}