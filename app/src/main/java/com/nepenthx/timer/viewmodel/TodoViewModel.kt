package com.nepenthx.timer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nepenthx.timer.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database.todoDao(), database.checkInDao())
    }

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.MONTH)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // 获取所有待办
    val allTodos: Flow<List<TodoItem>> = repository.getAllTodos()

    // 获取选中日期的待办
    val todosForSelectedDate: Flow<List<TodoItem>> = _selectedDate.flatMapLatest { date ->
        repository.getTodosByDate(date)
    }

    // 获取周期性待办
    val recurringTodos: Flow<List<TodoItem>> = repository.getRecurringTodos()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun getTodosByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TodoItem>> {
        return repository.getTodosByDateRange(startDate, endDate)
    }

    fun addTodo(
        title: String,
        note: String = "",
        priority: Priority = Priority.MEDIUM,
        dueDateTime: LocalDateTime,
        recurringType: RecurringType = RecurringType.NONE,
        recurringEndDate: LocalDateTime? = null
    ) {
        viewModelScope.launch {
            val todo = TodoItem(
                title = title,
                note = note,
                priority = priority,
                dueDateTime = dueDateTime,
                recurringType = recurringType,
                recurringEndDate = recurringEndDate
            )
            repository.insertTodo(todo)
        }
    }

    fun updateTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todo)
        }
    }

    fun toggleTodoCompletion(todo: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }

    // 打卡相关
    fun getCheckInsByTodoId(todoId: Long): Flow<List<CheckInRecord>> {
        return repository.getCheckInsByTodoId(todoId)
    }

    fun checkIn(todoId: Long, date: LocalDate = LocalDate.now(), note: String = "") {
        viewModelScope.launch {
            val checkIn = CheckInRecord(
                todoId = todoId,
                checkInDate = date,
                note = note
            )
            repository.insertCheckIn(checkIn)
        }
    }

    fun cancelCheckIn(todoId: Long, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            repository.deleteCheckInByTodoIdAndDate(todoId, date)
        }
    }

    fun getCheckInCountByTodoId(todoId: Long): Flow<Int> {
        return repository.getCheckInCountByTodoId(todoId)
    }
}

enum class ViewMode {
    DAY,
    WEEK,
    MONTH
}
