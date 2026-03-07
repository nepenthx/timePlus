/**
 * 主视图模型
 *
 * 本文件定义了应用的主 ViewModel，是 MVVM 架构的核心组件。
 * ViewModel 作为 UI 层和数据层之间的桥梁，负责管理 UI 状态和处理业务逻辑。
 *
 * 主要功能模块：
 * 1. 主题设置管理 - 管理应用主题预设和自定义颜色
 * 2. 排序模式管理 - 管理待办列表的排序方式
 * 3. 日期和视图模式 - 管理当前选中的日期和日历视图模式
 * 4. 待办事项管理 - 待办的增删改查和完成状态切换
 * 5. 子任务管理 - 子任务的增删改和完成状态切换
 * 6. 打卡功能 - 习惯追踪的打卡和取消打卡
 * 7. 标签管理 - 标签的增删改
 * 8. 数据导入导出 - 支持 iCal 和 JSON 格式
 * 9. 通知管理 - 待办提醒通知的调度
 *
 * 架构说明：
 * - 继承自 AndroidViewModel，可获取 Application 上下文
 * - 使用 StateFlow 暴露状态，实现响应式 UI 更新
 * - 使用 viewModelScope 管理协程，自动处理生命周期
 *
 * @author nepenthx
 * @since 1.0
 */
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

/**
 * 待办事项主视图模型
 *
 * 管理应用的所有业务逻辑和 UI 状态，是 UI 层与数据层的桥梁。
 *
 * @property application 应用上下文
 */
class TodoViewModel(application: Application) : AndroidViewModel(application) {
    /** 数据仓库实例 */
    private val repository: TodoRepository
    
    /** 主题偏好设置管理器 */
    val themePreferences: ThemePreferences

    init {
        // 初始化数据库和仓库
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database.todoDao(), database.checkInDao(), database.tagDao(), database.subTaskDao())
        themePreferences = ThemePreferences(application)
    }
    
    // ==================== 主题设置相关 ====================
    
    /** 当前主题设置的状态流 */
    val themeSettings: StateFlow<ThemeSettings> = themePreferences.themeSettings
    
    // ==================== 排序模式相关 ====================
    
    /** 排序模式的内部可变状态 */
    private val _sortMode = MutableStateFlow(themePreferences.getSortMode())
    
    /** 排序模式的公开状态流 */
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()
    
    /**
     * 设置排序模式
     *
     * @param mode 新的排序模式
     */
    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        themePreferences.saveSortMode(mode)
    }
    
    /**
     * 设置主题预设
     *
     * @param preset 选择的主题预设
     */
    fun setThemePreset(preset: ThemePreset) {
        themePreferences.savePreset(preset)
    }
    
    /**
     * 设置自定义主题颜色
     *
     * @param primaryColor 主色调（可选）
     * @param secondaryColor 次要色调（可选）
     * @param backgroundColor 背景色（可选）
     * @param surfaceColor 表面色（可选）
     * @param cardColor 卡片色（可选）
     * @param textColor 文字色（可选）
     * @param calendarColor 日历色（可选）
     * @param dateColor 日期色（可选）
     * @param gradientEnabled 是否启用渐变（可选）
     * @param gradientStartColor 渐变起始色（可选）
     * @param gradientEndColor 渐变结束色（可选）
     */
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

    // ==================== 日期和视图模式相关 ====================
    
    /** 当前选中日期的内部可变状态，默认为今天 */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    
    /** 当前选中日期的公开状态流 */
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /** 当前视图模式的内部可变状态，默认为周视图 */
    private val _viewMode = MutableStateFlow(ViewMode.WEEK)
    
    /** 当前视图模式的公开状态流 */
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // ==================== 待办事项数据流 ====================
    
    /** 获取所有待办事项的流 */
    val allTodos: Flow<List<TodoItem>> = repository.getAllTodos()

    /**
     * 获取选中日期的待办事项（包括周期性待办）
     *
     * 使用 flatMapLatest 和 combine 组合当天待办和周期性待办，
     * 过滤出在选中日期应该显示的周期性待办。
     */
    val todosForSelectedDate: Flow<List<TodoItem>> = _selectedDate.flatMapLatest { date ->
        // 获取当天的待办和所有重复待办
        combine(
            repository.getTodosByDate(date),
            repository.getRecurringTodos(),
            repository.getCheckInsByDate(date)
        ) { dateTodos, recurringTodos, checkIns ->
            val completedRecurringTodoIds = checkIns.map { it.todoId }.toSet()

            // 合并当天的待办和在该日期应该显示的重复待办
            val recurringForDate = recurringTodos.filter { todo ->
                // 排除已经在dateTodos中的待办（避免重复）
                todo.id !in dateTodos.map { it.id } && todo.shouldShowOnDate(date)
            }.map { recurringTodo ->
                recurringTodo.copy(isCompleted = recurringTodo.id in completedRecurringTodoIds)
                    .copy(
                        dueDateTime = recurringTodo.dueDateTime
                            .withYear(date.year)
                            .withMonth(date.monthValue)
                            .withDayOfMonth(date.dayOfMonth)
                    )
            }

            (dateTodos.map { todo ->
                if (todo.isRecurring) {
                    todo.copy(isCompleted = todo.id in completedRecurringTodoIds)
                } else {
                    todo
                }
            } + recurringForDate).sortedBy { it.dueDateTime }
        }
    }

    /** 获取所有周期性待办事项的流 */
    val recurringTodos: Flow<List<TodoItem>> = repository.getRecurringTodos()

    /**
     * 选择日期
     *
     * @param date 要选择的日期
     */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /**
     * 设置日历视图模式
     *
     * @param mode 视图模式（日/周/月）
     */
    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    /**
     * 获取日期范围内的待办事项（包括周期性待办）
     *
     * 用于周视图和月视图，合并范围内的待办和在该范围内应该显示的周期性待办。
     * 对于周期性待办，会为每个应该显示的日期创建虚拟副本。
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期范围内的待办事项流
     */
    fun getTodosByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TodoItem>> {
        return combine(
            repository.getTodosByDateRange(startDate, endDate),
            repository.getRecurringTodos(),
            repository.getCheckInsByDateRange(startDate, endDate)
        ) { rangeTodos, recurringTodos, checkIns ->
            val completedRecurringKeys = checkIns.map { it.todoId to it.checkInDate }.toSet()

            // 合并范围内的待办和在该范围内应该显示的重复待办
            val allTodos = rangeTodos.map { todo ->
                if (todo.isRecurring) {
                    val date = todo.dueDateTime.toLocalDate()
                    todo.copy(isCompleted = (todo.id to date) in completedRecurringKeys)
                } else {
                    todo
                }
            }.toMutableList()
            
            recurringTodos.forEach { recurringTodo ->
                // 检查每一天是否应该显示这个重复待办
                var currentDate = startDate
                while (!currentDate.isAfter(endDate)) {
                    if (recurringTodo.shouldShowOnDate(currentDate)) {
                        // 创建一个虚拟的待办副本，时间设置为当前日期
                        val virtualTodo = recurringTodo.copy(
                            dueDateTime = recurringTodo.dueDateTime.withYear(currentDate.year)
                                .withMonth(currentDate.monthValue)
                                .withDayOfMonth(currentDate.dayOfMonth),
                            isCompleted = (recurringTodo.id to currentDate) in completedRecurringKeys
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

    // ==================== 待办事项管理 ====================
    
    /**
     * 添加新的待办事项
     *
     * 创建待办事项并插入数据库，如果开启了通知则安排提醒。
     *
     * @param title 待办标题
     * @param note 备注内容
     * @param priority 优先级
     * @param dueDateTime 截止日期时间
     * @param recurringType 周期类型
     * @param recurringEndDate 周期结束日期
     * @param customWeekDays 自定义周天位掩码
     * @param tagId 关联的标签ID
     * @param enableNotification 是否启用通知
     * @param notifyMinutesBefore 提前通知分钟数
     * @param hasSubTasks 是否包含子任务
     */
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
    
    /**
     * 安排待办通知
     *
     * 调用 NotificationHelper 为待办事项安排提醒通知。
     *
     * @param todo 需要安排通知的待办事项
     */
    private fun scheduleNotification(todo: TodoItem) {
        NotificationHelper.scheduleNotification(getApplication(), todo)
    }

    // ==================== 子任务管理 ====================
    
    /**
     * 获取指定待办的所有子任务
     *
     * @param todoId 待办事项ID
     * @return 子任务列表的 Flow
     */
    fun getSubTasks(todoId: Long): Flow<List<SubTask>> = repository.getSubTasksByTodoId(todoId)

    /**
     * 添加子任务
     *
     * @param todoId 父待办事项ID
     * @param title 子任务标题
     */
    fun addSubTask(todoId: Long, title: String) {
        viewModelScope.launch {
            repository.insertSubTask(SubTask(todoId = todoId, title = title))
        }
    }

    /**
     * 切换子任务的完成状态
     *
     * @param subTask 要切换状态的子任务
     */
    fun toggleSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.updateSubTask(subTask.copy(isCompleted = !subTask.isCompleted))
        }
    }

    /**
     * 删除子任务
     *
     * @param subTask 要删除的子任务
     */
    fun deleteSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.deleteSubTask(subTask)
        }
    }

    // ==================== 待办事项更新操作 ====================
    
    /**
     * 更新待办事项
     *
     * @param todo 要更新的待办事项
     */
    fun updateTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todo)
        }
    }

    /**
     * 切换待办事项的完成状态
     *
     * @param todo 要切换状态的待办事项
     */
    fun toggleTodoCompletion(todo: TodoItem) {
        viewModelScope.launch {
            if (todo.isRecurring) {
                val targetDate = todo.dueDateTime.toLocalDate()
                if (todo.isCompleted) {
                    repository.deleteCheckInByTodoIdAndDate(todo.id, targetDate)
                } else {
                    repository.insertCheckIn(
                        CheckInRecord(
                            todoId = todo.id,
                            checkInDate = targetDate
                        )
                    )
                }
            } else {
                repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
            }
        }
    }

    /**
     * 删除待办事项
     *
     * @param todo 要删除的待办事项
     */
    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }

    // ==================== 打卡功能 ====================
    
    /**
     * 获取指定待办的所有打卡记录
     *
     * @param todoId 待办事项ID
     * @return 打卡记录列表的 Flow
     */
    fun getCheckInsByTodoId(todoId: Long): Flow<List<CheckInRecord>> {
        return repository.getCheckInsByTodoId(todoId)
    }

    /**
     * 打卡
     *
     * 为指定待办在指定日期创建打卡记录。
     *
     * @param todoId 待办事项ID
     * @param date 打卡日期，默认为今天
     * @param note 打卡备注
     */
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

    /**
     * 取消打卡
     *
     * 删除指定待办在指定日期的打卡记录。
     *
     * @param todoId 待办事项ID
     * @param date 要取消的日期，默认为今天
     */
    fun cancelCheckIn(todoId: Long, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            repository.deleteCheckInByTodoIdAndDate(todoId, date)
        }
    }

    /**
     * 获取指定待办的打卡总次数
     *
     * @param todoId 待办事项ID
     * @return 打卡次数的 Flow
     */
    fun getCheckInCountByTodoId(todoId: Long): Flow<Int> {
        return repository.getCheckInCountByTodoId(todoId)
    }
    
    // ==================== 标签管理 ====================
    
    /** 获取所有标签的流 */
    val allTags: Flow<List<TodoTag>> = repository.getAllTags()
    
    /**
     * 添加标签
     *
     * @param name 标签名称
     * @param color 标签颜色，默认为主题紫色
     */
    fun addTag(name: String, color: Long = 0xFF6750A4) {
        viewModelScope.launch {
            repository.insertTag(TodoTag(name = name, color = color))
        }
    }
    
    /**
     * 更新标签
     *
     * @param tag 要更新的标签
     */
    fun updateTag(tag: TodoTag) {
        viewModelScope.launch {
            repository.updateTag(tag)
        }
    }
    
    /**
     * 删除标签
     *
     * @param tag 要删除的标签
     */
    fun deleteTag(tag: TodoTag) {
        viewModelScope.launch {
            repository.deleteTag(tag)
        }
    }
    
    // ==================== 数据导入导出 ====================
    
    /**
     * 导出为 iCal 格式
     *
     * 将所有待办事项和标签导出为 iCalendar (.ics) 格式字符串。
     * iCal 格式是通用的日历数据交换格式，可导入到其他日历应用。
     *
     * @return iCal 格式的字符串
     */
    suspend fun exportToICal(): String {
        val todos = repository.getAllTodos().first()
        val tags = repository.getAllTags().first()
        return com.nepenthx.timer.export.DataExportHelper.exportToICal(todos, tags)
    }
    
    /**
     * 导出为 JSON 格式
     *
     * 将所有待办事项、标签和子任务导出为 JSON 格式字符串。
     * JSON 格式保留了完整的数据结构，适合在本应用间进行数据迁移。
     *
     * @return JSON 格式的字符串
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
     * 从 iCal 格式导入
     *
     * 解析 iCal 格式字符串并导入待办事项。
     * 如果待办开启了通知，会自动安排提醒。
     *
     * @param content iCal 格式的字符串
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
     * 从 JSON 格式导入
     *
     * 解析 JSON 格式字符串并导入待办事项、标签和子任务。
     * 会正确处理标签ID的映射关系，确保待办与标签的关联正确。
     *
     * 导入流程：
     * 1. 先导入所有标签，建立旧ID到新ID的映射
     * 2. 导入待办事项，使用映射更新标签ID
     * 3. 导入关联的子任务
     * 4. 为开启通知的待办安排提醒
     *
     * @param content JSON 格式的字符串
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

/**
 * 日历视图模式枚举
 *
 * 定义日历界面的三种显示模式。
 *
 * - DAY: 日视图，显示单日的待办
 * - WEEK: 周视图，显示一周的待办
 * - MONTH: 月视图，显示一月的待办
 */
enum class ViewMode {
    DAY,
    WEEK,
    MONTH
}
