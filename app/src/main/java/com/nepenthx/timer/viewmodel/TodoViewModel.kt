package com.nepenthx.timer.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nepenthx.timer.data.*
import com.nepenthx.timer.export.DataExportHelper
import com.nepenthx.timer.notification.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository
    val themePreferences: ThemePreferences

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database.todoDao(), database.checkInDao(), database.tagDao(), database.subTaskDao())
        themePreferences = ThemePreferences(application)
    }
    
    // 主题设置
    val themeSettings: StateFlow<ThemeSettings> = themePreferences.themeSettings
    
    // 排序模式
    private val _sortMode = MutableStateFlow(themePreferences.getSortMode())
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()
    
    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        themePreferences.saveSortMode(mode)
    }
    
    fun setThemePreset(preset: ThemePreset) {
        themePreferences.savePreset(preset)
    }
    
    fun setCustomColor(
        primaryColor: Long? = null,
        secondaryColor: Long? = null,
        backgroundColor: Long? = null,
        surfaceColor: Long? = null,
        cardColor: Long? = null,
        textColor: Long? = null,
        calendarColor: Long? = null,
        dateColor: Long? = null,
        gradientEnabled: Boolean? = null,
        gradientStartColor: Long? = null,
        gradientEndColor: Long? = null
    ) {
        themePreferences.saveCustomColor(
            primaryColor, secondaryColor, backgroundColor, surfaceColor,
            cardColor, textColor, calendarColor, dateColor,
            gradientEnabled, gradientStartColor, gradientEndColor
        )
    }

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.MONTH)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // 获取所有待办
    val allTodos: Flow<List<TodoItem>> = repository.getAllTodos()

    // 获取选中日期的待办（包括重复待办）
    val todosForSelectedDate: Flow<List<TodoItem>> = _selectedDate.flatMapLatest { date ->
        // 获取当天的待办和所有重复待办
        combine(
            repository.getTodosByDate(date),
            repository.getRecurringTodos()
        ) { dateTodos, recurringTodos ->
            // 合并当天的待办和在该日期应该显示的重复待办
            val recurringForDate = recurringTodos.filter { todo ->
                // 排除已经在dateTodos中的待办（避免重复）
                todo.id !in dateTodos.map { it.id } && todo.shouldShowOnDate(date)
            }
            (dateTodos + recurringForDate).sortedBy { it.dueDateTime }
        }
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
        return combine(
            repository.getTodosByDateRange(startDate, endDate),
            repository.getRecurringTodos()
        ) { rangeTodos, recurringTodos ->
            // 合并范围内的待办和在该范围内应该显示的重复待办
            val allTodos = rangeTodos.toMutableList()
            
            recurringTodos.forEach { recurringTodo ->
                // 检查每一天是否应该显示这个重复待办
                var currentDate = startDate
                while (!currentDate.isAfter(endDate)) {
                    if (recurringTodo.shouldShowOnDate(currentDate)) {
                        // 创建一个虚拟的待办副本，时间设置为当前日期
                        val virtualTodo = recurringTodo.copy(
                            dueDateTime = recurringTodo.dueDateTime.withYear(currentDate.year)
                                .withMonth(currentDate.monthValue)
                                .withDayOfMonth(currentDate.dayOfMonth)
                        )
                        // 避免重复添加
                        if (allTodos.none { it.id == virtualTodo.id && it.dueDateTime.toLocalDate() == currentDate }) {
                            allTodos.add(virtualTodo)
                        }
                    }
                    currentDate = currentDate.plusDays(1)
                }
            }
            
            allTodos.sortedBy { it.dueDateTime }
        }
    }

    fun addTodo(
        title: String,
        note: String = "",
        priority: Priority = Priority.MEDIUM,
        dueDateTime: LocalDateTime,
        recurringType: RecurringType = RecurringType.NONE,
        recurringEndDate: LocalDateTime? = null,
        customWeekDays: Int = 0,
        tagId: Long? = null,
        enableNotification: Boolean = false,
        notifyMinutesBefore: Int = 15,
        hasSubTasks: Boolean = false
    ) {
        viewModelScope.launch {
            val todo = TodoItem(
                title = title,
                note = note,
                priority = priority,
                dueDateTime = dueDateTime,
                recurringType = recurringType,
                recurringEndDate = recurringEndDate,
                customWeekDays = customWeekDays,
                tagId = tagId,
                enableNotification = enableNotification,
                notifyMinutesBefore = notifyMinutesBefore,
                hasSubTasks = hasSubTasks
            )
            val todoId = repository.insertTodo(todo)
            
            // 如果开启了通知，安排通知
            if (enableNotification) {
                scheduleNotification(todo.copy(id = todoId))
            }
        }
    }
    
    // 安排通知
    private fun scheduleNotification(todo: TodoItem) {
        NotificationHelper.scheduleNotification(getApplication(), todo)
    }

    // 子任务相关
    fun getSubTasks(todoId: Long): Flow<List<SubTask>> = repository.getSubTasksByTodoId(todoId)

    fun addSubTask(todoId: Long, title: String) {
        viewModelScope.launch {
            repository.insertSubTask(SubTask(todoId = todoId, title = title))
        }
    }

    fun toggleSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.updateSubTask(subTask.copy(isCompleted = !subTask.isCompleted))
        }
    }

    fun deleteSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.deleteSubTask(subTask)
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
    
    // 标签相关
    val allTags: Flow<List<TodoTag>> = repository.getAllTags()
    
    fun addTag(name: String, color: Long = 0xFF6750A4) {
        viewModelScope.launch {
            repository.insertTag(TodoTag(name = name, color = color))
        }
    }
    
    fun updateTag(tag: TodoTag) {
        viewModelScope.launch {
            repository.updateTag(tag)
        }
    }
    
    fun deleteTag(tag: TodoTag) {
        viewModelScope.launch {
            repository.deleteTag(tag)
        }
    }
    
    // ==================== 数据导入导出 ====================
    
    /**
     * 导出为iCal格式
     */
    suspend fun exportToICal(): String {
        val todos = repository.getAllTodos().first()
        val tags = repository.getAllTags().first()
        return com.nepenthx.timer.export.DataExportHelper.exportToICal(todos, tags)
    }
    
    /**
     * 导出为JSON格式
     */
    suspend fun exportToJson(): String {
        val todos = repository.getAllTodos().first()
        val tags = repository.getAllTags().first()
        
        // 获取所有包含子任务的待办的子任务
        val subTasksMap = mutableMapOf<Long, List<SubTask>>()
        todos.forEach { todo ->
            if (todo.hasSubTasks) {
                val subTasks = repository.getSubTasksByTodoId(todo.id).first()
                if (subTasks.isNotEmpty()) {
                    subTasksMap[todo.id] = subTasks
                }
            }
        }
        
        return com.nepenthx.timer.export.DataExportHelper.exportToJson(todos, tags, subTasksMap)
    }
    
    /**
     * 从iCal格式导入
     */
    fun importFromICal(content: String) {
        viewModelScope.launch {
            val todos = com.nepenthx.timer.export.DataExportHelper.importFromICal(content)
            todos.forEach { todo ->
                val todoId = repository.insertTodo(todo)
                if (todo.enableNotification) {
                    scheduleNotification(todo.copy(id = todoId))
                }
            }
        }
    }
    
    /**
     * 从JSON格式导入
     */
    fun importFromJson(content: String) {
        viewModelScope.launch {
            val (todoPairs, tags) = com.nepenthx.timer.export.DataExportHelper.importFromJson(content)
            
            // 先导入标签，建立ID映射
            val tagIdMap = mutableMapOf<Long, Long>()
            tags.forEach { tag ->
                val newId = repository.insertTag(tag)
                tagIdMap[tag.id] = newId
            }
            
            // 导入待办及其子任务
            todoPairs.forEach { (todo, subTasks) ->
                val newTagId = todo.tagId?.let { tagIdMap[it] }
                val updatedTodo = todo.copy(tagId = newTagId)
                val todoId = repository.insertTodo(updatedTodo)
                
                // 导入关联的子任务
                subTasks.forEach { subTask ->
                    repository.insertSubTask(subTask.copy(todoId = todoId))
                }
                
                if (updatedTodo.enableNotification) {
                    scheduleNotification(updatedTodo.copy(id = todoId))
                }
            }
        }
    }
}

enum class ViewMode {
    DAY,
    WEEK,
    MONTH
}
