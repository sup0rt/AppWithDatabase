package com.example.appwithdatabase.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appwithdatabase.R
import com.example.appwithdatabase.data.entities.TaskEntity
import com.example.appwithdatabase.databinding.TaskItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val onTaskChecked: (TaskEntity, Boolean) -> Unit,
    private val onTaskClicked: (TaskEntity) -> Unit,
    private val onTaskLongClicked: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    private fun getPriorityIcon(priority: TaskEntity.Priority): Int {
        return when (priority) {
            TaskEntity.Priority.HIGH -> R.drawable.ic_priority_high
            TaskEntity.Priority.MEDIUM -> R.drawable.ic_priority_medium
            TaskEntity.Priority.LOW -> R.drawable.ic_priority_low
        }
    }

    inner class TaskViewHolder(
        private val binding: TaskItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskEntity) {
            binding.apply {
                textTaskTitle.text = task.title
                textTaskDescription.text = task.description ?: "Нет описания"
                textTaskDescription.visibility = if (task.description.isNullOrEmpty()) View.GONE else View.VISIBLE
                checkBoxIsCompleted.isChecked = task.isCompleted

                // Устанавливаем иконку приоритета
                imagePriority.setImageResource(getPriorityIcon(task.priority))

                // Зачеркиваем текст если задача выполнена
                if (task.isCompleted) {
                    textTaskTitle.paintFlags = android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    textTaskDescription.paintFlags = android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    textTaskTitle.paintFlags = 0
                    textTaskDescription.paintFlags = 0
                }

                val priorityText = when (task.priority) {
                    TaskEntity.Priority.LOW -> "Низкий"
                    TaskEntity.Priority.MEDIUM -> "Средний"
                    TaskEntity.Priority.HIGH -> "Высокий"
                }
                textPriority.text = priorityText

                textDueDate.text = if (task.dueDate != null) {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(task.dueDate)
                } else {
                    "Без срока"
                }

                // Обработчики кликов
                checkBoxIsCompleted.setOnCheckedChangeListener { _, isChecked ->
                    onTaskChecked(task, isChecked)
                }

                root.setOnClickListener {
                    onTaskClicked(task)
                }

                root.setOnLongClickListener {
                    onTaskLongClicked(task)
                    true
                }
            }
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
    override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
        return oldItem == newItem
    }
}