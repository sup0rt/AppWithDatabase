package com.example.appwithdatabase.data.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.appwithdatabase.data.entities.CategoryEntity
import com.example.appwithdatabase.data.entities.TaskEntity

data class TaskWithCategory(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "category_id"
    )
    val category: CategoryEntity
)
