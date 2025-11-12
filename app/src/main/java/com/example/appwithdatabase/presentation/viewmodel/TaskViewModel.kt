package com.example.appwithdatabase.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appwithdatabase.data.entities.TaskEntity
import com.example.appwithdatabase.domain.models.TaskStatistics
import com.example.appwithdatabase.domain.models.TaskUiState
import com.example.appwithdatabase.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow(-1L)

    // Основной Flow задач с фильтрацией по поиску и категории
    val tasks: Flow<List<TaskEntity>> = combine(
        taskRepository.getAllTasks(),
        _searchQuery,
        _categoryFilter
    ) { tasks, query, categoryId ->
        tasks.filter { task ->
            val matchesQuery = query.isEmpty() ||
                    task.title.contains(query, true) ||
                    task.description?.contains(query, true) == true
            val matchesCategory = categoryId == -1L || task.categoryId == categoryId
            matchesQuery && matchesCategory
        }
    }.flowOn(Dispatchers.Default)

    // Категории
    val categories = taskRepository.getAllCategories()

    // Состояние UI
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            try {
                _uiState.value = TaskUiState.Loading
                // Просто подписываемся на Flow задач
                _uiState.value = TaskUiState.Success
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: Long) {
        _categoryFilter.value = categoryId
    }

    // Создание задачи - исправлено: убрана suspend и добавлен launch
    fun createTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                taskRepository.createTask(task)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    // Обновление задачи
    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    // Обновление статуса выполнения
    fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskCompletion(taskId, isCompleted)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    // Удаление задачи
    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(task)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }


    fun getStatistics(): Flow<TaskStatistics> {
        return flow {
            emit(taskRepository.getTaskStatistics())
        }
    }
}