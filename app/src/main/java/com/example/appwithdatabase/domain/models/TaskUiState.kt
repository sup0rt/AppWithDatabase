package com.example.appwithdatabase.domain.models

sealed class TaskUiState{
    object Loading: TaskUiState()
    object Success: TaskUiState()
    data class Error(
        val message: String
    ): TaskUiState()
}