package com.example.appwithdatabase.presentation.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appwithdatabase.R
import com.example.appwithdatabase.data.entities.TaskEntity
import com.example.appwithdatabase.databinding.FragmentTaskListBinding
import com.example.appwithdatabase.domain.models.TaskStatistics
import com.example.appwithdatabase.domain.models.TaskUiState
import com.example.appwithdatabase.presentation.adapters.TaskAdapter
import com.example.appwithdatabase.presentation.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupCategorySpinner()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskChecked = { task, isCompleted ->
                viewModel.updateTaskCompletion(task.id, isCompleted)
            },
            onTaskClicked = { task ->
                showCreateTaskDialog(task)
            },
            onTaskLongClicked = { task ->
                showDeleteConfirmation(task)
            }
        )

        binding.recyclerViewTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collect { tasks ->
                taskAdapter.submitList(tasks)
                updateEmptyState(tasks.isEmpty())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is TaskUiState.Loading -> showLoading()
                    is TaskUiState.Success -> hideLoading()
                    is TaskUiState.Error -> showError(state.message)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getStatistics().collect { statistics ->
                updateStatistics(statistics)
            }
        }
    }

    private fun setupCategorySpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                val categoryList = mutableListOf<Pair<Long, String>>().apply {
                    add(-1L to "Все категории")
                    addAll(categories.map { it.id to it.name })
                }

                // Создаем адаптер с кастомными layout
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,  // Layout для выбранного элемента
                    categoryList.map { it.second }
                ).apply {
                    // Устанавливаем отдельный layout для выпадающего списка
                    setDropDownViewResource(R.layout.spinner_dropdown_item)
                }

                binding.spinnerCategories.adapter = adapter

                binding.spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedCategoryId = categoryList[position].first
                        viewModel.setCategoryFilter(selectedCategoryId)

                        binding.buttonClearFilter.visibility =
                            if (selectedCategoryId != -1L) View.VISIBLE else View.GONE
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
        }
    }


    private fun setupClickListeners() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        binding.fabAddTask.setOnClickListener {
            showCreateTaskDialog()
        }

        binding.buttonClearFilter.setOnClickListener {
            binding.spinnerCategories.setSelection(0)
        }
    }

    fun showCreateTaskDialog(task: TaskEntity? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_task_editor, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(if (task == null) "Новая задача" else "Редактировать задачу")
            .create()

        val editTitle = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val editDescription = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val radioGroupPriority = dialogView.findViewById<RadioGroup>(R.id.radioGroupPriority)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val buttonDueDate = dialogView.findViewById<Button>(R.id.buttonDueDate)
        val textSelectedDate = dialogView.findViewById<TextView>(R.id.textSelectedDate)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)

        // Заполнение данных если редактирование
        task?.let {
            editTitle.setText(it.title)
            editDescription.setText(it.description)
            when (it.priority) {
                TaskEntity.Priority.LOW -> radioGroupPriority.check(R.id.radioLow)
                TaskEntity.Priority.MEDIUM -> radioGroupPriority.check(R.id.radioMedium)
                TaskEntity.Priority.HIGH -> radioGroupPriority.check(R.id.radioHigh)
            }
            it.dueDate?.let { date ->
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                textSelectedDate.text = dateFormat.format(date)
            }
        }

        // Загрузка категорий в спиннер
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name }
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerCategory.adapter = adapter

                task?.let {
                    val category = categories.find { category -> category.id == it.categoryId }
                    val position = categories.indexOf(category)
                    if (position != -1) spinnerCategory.setSelection(position)
                }
            }
        }

        // Валидация названия
        editTitle.doAfterTextChanged {
            buttonSave.isEnabled = it?.toString()?.trim()?.isNotEmpty() == true
        }

        // Выбор даты
        var selectedDate: Date? = task?.dueDate
        buttonDueDate.setOnClickListener {
            showDatePickerDialog(textSelectedDate) { date ->
                selectedDate = date
            }
        }

        // Сохранение
        buttonSave.setOnClickListener {
            val title = editTitle.text.toString().trim()
            if (title.isEmpty()) {
                editTitle.error = "Введите название задачи"
                return@setOnClickListener
            }

            val description = editDescription.text.toString().trim()
            val selectedCategoryPosition = spinnerCategory.selectedItemPosition

            // Исправление: получаем категории через Flow.value
            var selectedCategoryId: Long = -1L
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.categories.collect { categories ->
                    if (categories.isNotEmpty() && selectedCategoryPosition < categories.size) {
                        selectedCategoryId = categories[selectedCategoryPosition].id

                        val priority = when (radioGroupPriority.checkedRadioButtonId) {
                            R.id.radioLow -> TaskEntity.Priority.LOW
                            R.id.radioHigh -> TaskEntity.Priority.HIGH
                            else -> TaskEntity.Priority.MEDIUM
                        }

                        if (task == null) {
                            // Создание новой задачи
                            val newTask = TaskEntity(
                                title = title,
                                description = if (description.isEmpty()) null else description,
                                priority = priority,
                                dueDate = selectedDate,
                                categoryId = selectedCategoryId,
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewModel.createTask(newTask)
                            }
                            Toast.makeText(requireContext(), "Задача создана", Toast.LENGTH_SHORT).show()
                        } else {
                            // Обновление существующей задачи
                            val updatedTask = task.copy(
                                title = title,
                                description = if (description.isEmpty()) null else description,
                                priority = priority,
                                dueDate = selectedDate,
                                categoryId = selectedCategoryId,
                                updatedAt = Date()
                            )
                            viewModel.updateTask(updatedTask)
                            Toast.makeText(requireContext(), "Задача обновлена", Toast.LENGTH_SHORT).show()
                        }

                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Нет доступных категорий", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave.isEnabled = task != null || editTitle.text.toString().trim().isNotEmpty()
        dialog.show()
    }

    private fun showDatePickerDialog(textView: TextView, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                textView.text = dateFormat.format(selectedDate.time)
                onDateSelected(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showDeleteConfirmation(task: TaskEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление задачи")
            .setMessage("Вы уверены, что хотите удалить задачу \"${task.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteTask(task)
                Toast.makeText(requireContext(), "Задача удалена", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewTasks.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun updateStatistics(statistics: TaskStatistics) {
        binding.textStatistics.text =
            "Задач: ${statistics.totalTasks} | Выполнено: ${statistics.completedTasks} (${"%.1f".format(statistics.completionRate)}%)"
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}