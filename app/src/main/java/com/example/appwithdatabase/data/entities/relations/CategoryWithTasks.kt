package com.example.appwithdatabase.data.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.appwithdatabase.data.entities.CategoryEntity
import com.example.appwithdatabase.data.entities.TaskEntity

data class CategoryWithTasks(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "category_id"
    )
    val tasks: List<TaskEntity>
)
