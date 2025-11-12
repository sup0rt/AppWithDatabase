package com.example.appwithdatabase.domain.models

data class TaskStatistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Double
)
