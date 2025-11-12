package com.example.appwithdatabase.data.repository

import com.example.appwithdatabase.data.dao.CategoryDao
import com.example.appwithdatabase.data.dao.TaskDao
import com.example.appwithdatabase.data.entities.CategoryEntity
import com.example.appwithdatabase.data.entities.TaskEntity
import com.example.appwithdatabase.domain.models.TaskStatistics
import com.example.appwithdatabase.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) : TaskRepository {

    // === Методы для задач ===
    override fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    override fun getTasksByCategory(categoryId: Long): Flow<List<TaskEntity>> =
        taskDao.getTasksByCategory(categoryId)

    override suspend fun createTask(task: TaskEntity): Long = taskDao.insertTask(task)

    override suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    override suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    override fun searchTasks(query: String): Flow<List<TaskEntity>> = taskDao.searchTasks(query)

    override suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) =
        taskDao.updatetTaskCompletion(taskId, isCompleted)

    override fun getTaskStatistics(): TaskStatistics {
        // Используем runBlocking для вызова suspend функций из не-suspend контекста
        return kotlinx.coroutines.runBlocking {
            val totalTasks = taskDao.getTotalTaskCount()
            val completedTasks = taskDao.getCompletedTaskCount()

            TaskStatistics(
                totalTasks = totalTasks,
                completedTasks = completedTasks,
                completionRate = if (totalTasks > 0) {
                    (completedTasks.toDouble() / totalTasks) * 100
                } else 0.0
            )
        }
    }

    // === Методы для категорий ===
    override fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    override suspend fun getCategoryById(categoryId: Long): CategoryEntity? =
        categoryDao.getCategoryById(categoryId)

    override suspend fun createCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    override suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    override suspend fun deleteCategory(categoryId: Long) =
        categoryDao.deleteCategoryById(categoryId)
}