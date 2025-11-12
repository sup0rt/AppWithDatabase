package com.example.appwithdatabase.di

import android.content.Context
import com.example.appwithdatabase.data.dao.CategoryDao
import com.example.appwithdatabase.data.dao.TaskDao
import com.example.appwithdatabase.data.database.AppDatabase
import com.example.appwithdatabase.data.repository.TaskRepositoryImpl
import com.example.appwithdatabase.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }

    @Provides
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        categoryDao: CategoryDao
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao, categoryDao)
    }
}