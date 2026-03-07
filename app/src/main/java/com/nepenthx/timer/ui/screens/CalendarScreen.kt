/**
 * 日历屏幕
 *
 * 本文件定义了应用的日历视图界面，是应用的主界面之一。
 * 提供日/周/月三种视图模式，显示选中日期的待办列表。
 *
 * 主要功能：
 * - 日历视图切换（日/周/月）
 * - 日期选择和导航
 * - 显示选中日期的待办事项列表
 * - 支持按排序模式显示待办
 *
 * 界面布局：
 * - 顶部：标题栏 + 视图切换按钮
 * - 中部：日历视图（占1/4屏幕）
 * - 底部：待办列表（占剩余空间）
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nepenthx.timer.data.SortMode
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.components.*
import com.nepenthx.timer.ui.theme.LocalAppColors
import com.nepenthx.timer.viewmodel.TodoViewModel
import com.nepenthx.timer.viewmodel.ViewMode
import java.time.LocalDate
import java.time.YearMonth

/**
 * 日历屏幕组件
 *
 * 显示日历视图和当日待办列表，支持日/周/月三种视图模式。
 *
 * @param viewModel 待办视图模型
 * @param onTodoClick 待办点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: TodoViewModel,
    onTodoClick: (TodoItem) -> Unit
) {
    val appColors = LocalAppColors.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val todosForSelectedDate by viewModel.todosForSelectedDate.collectAsState(initial = emptyList())
    val sortMode by viewModel.sortMode.collectAsState()

    /** 月视图当前显示的年月 */
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    // ==================== 数据加载 ====================
    /** 月视图待办数据 */
    val monthTodos by remember(currentYearMonth) {
        val start = currentYearMonth.atDay(1)
        val end = currentYearMonth.atEndOfMonth()
        viewModel.getTodosByDateRange(start, end)
    }.collectAsState(initial = emptyList())

    /** 月视图待办按日期分组的映射 */
    val monthTodosMap = remember(monthTodos) {
        monthTodos.groupBy { it.dueDateTime.toLocalDate() }
    }

    /** 周视图待办数据 */
    val weekStart = remember(selectedDate) {
        selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() % 7)
    }
    val weekEnd = remember(weekStart) { weekStart.plusDays(6) }
    val weekTodos by remember(weekStart) {
        viewModel.getTodosByDateRange(weekStart, weekEnd)
    }.collectAsState(initial = emptyList())

    /** 周视图待办按日期分组的映射 */
    val weekTodosMap = remember(weekTodos) {
        weekTodos.groupBy { it.dueDateTime.toLocalDate() }
    }

    // 获取所有子任务数据
    val allSubTasksMap = remember(todosForSelectedDate) {
        derivedStateOf {
            val map = mutableMapOf<Long, List<SubTask>>()
            todosForSelectedDate.forEach { todo ->
                // 这里需要异步获取，暂时先留空
            }
            map
        }
    }

    // ==================== 界面布局 ====================
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "待办日历",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.text
            )

            // 视图模式切换按钮
            FilledTonalButton(
                onClick = {
                    viewModel.setViewMode(
                        when (viewMode) {
                            ViewMode.MONTH -> ViewMode.WEEK
                            ViewMode.WEEK -> ViewMode.DAY
                            ViewMode.DAY -> ViewMode.MONTH
                        }
                    )
                }
            ) {
                Icon(
                    imageVector = when (viewMode) {
                        ViewMode.MONTH -> Icons.Default.DateRange
                        ViewMode.WEEK -> Icons.Default.List
                        ViewMode.DAY -> Icons.Default.Info
                    },
                    contentDescription = "切换视图",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (viewMode) {
                        ViewMode.MONTH -> "月"
                        ViewMode.WEEK -> "周"
                        ViewMode.DAY -> "日"
                    },
                    fontSize = 12.sp
                )
            }
        }

        // 日历视图 - 占屏幕1/4
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = appColors.card.copy(alpha = 0.6f)
            )
        ) {
            when (viewMode) {
                ViewMode.MONTH -> {
                    Column {
                        // 月份导航
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                currentYearMonth = currentYearMonth.minusMonths(1)
                            }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "上个月",
                                    tint = appColors.calendar,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            TextButton(onClick = {
                                currentYearMonth = YearMonth.now()
                                viewModel.selectDate(LocalDate.now())
                            }) {
                                Text("今天", color = appColors.calendar, fontSize = 12.sp)
                            }

                            IconButton(onClick = {
                                currentYearMonth = currentYearMonth.plusMonths(1)
                            }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "下个月",
                                    tint = appColors.calendar,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        MonthCalendarView(
                            yearMonth = currentYearMonth,
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                viewModel.selectDate(date)
                                currentYearMonth = YearMonth.from(date)
                            },
                            todosMap = monthTodosMap,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                ViewMode.WEEK -> {
                    Column {
                        // 周导航
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                viewModel.selectDate(selectedDate.minusDays(7))
                            }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "上一周",
                                    tint = appColors.calendar,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            TextButton(onClick = {
                                viewModel.selectDate(LocalDate.now())
                            }) {
                                Text("本周", color = appColors.calendar, fontSize = 12.sp)
                            }

                            IconButton(onClick = {
                                viewModel.selectDate(selectedDate.plusDays(7))
                            }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "下一周",
                                    tint = appColors.calendar,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        WeekCalendarView(
                            selectedDate = selectedDate,
                            onDateSelected = { viewModel.selectDate(it) },
                            todosMap = weekTodosMap,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                ViewMode.DAY -> {
                    Column {
                        // 日导航
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                viewModel.selectDate(selectedDate.minusDays(1))
                            }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "前一天",
                                    tint = appColors.calendar,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            TextButton(onClick = {
                                viewModel.selectDate(LocalDate.now())
                            }) {
                                Text("今天", color = appColors.calendar, fontSize = 12.sp)
                            }

                            IconButton(onClick = {
                                viewModel.selectDate(selectedDate.plusDays(1))
                            }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "后一天",
                                    tint = appColors.calendar,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DayCalendarView(
                            selectedDate = selectedDate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 待办列表标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "待办事项",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.text
            )

            Text(
                text = "${todosForSelectedDate.count { !it.isCompleted }}项待完成",
                fontSize = 12.sp,
                color = appColors.text.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 待办列表 - 按分组模式显示，占剩余空间
        GroupedTodoList(
            todos = todosForSelectedDate,
            sortMode = sortMode,
            onTodoClick = onTodoClick,
            onToggleComplete = { todo ->
                viewModel.toggleTodoCompletion(todo)
            },
            onDeleteTodo = { todo ->
                viewModel.deleteTodo(todo)
            },
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 分组待办列表组件
 *
 * 根据排序模式显示待办列表，支持按时间排序或按优先级分组显示。
 *
 * @param todos 待办列表
 * @param sortMode 排序模式
 * @param onTodoClick 待办点击回调
 * @param onToggleComplete 切换完成状态回调
 * @param onDeleteTodo 删除待办回调
 * @param viewModel 视图模型
 * @param modifier 修饰符
 */
@Composable
private fun GroupedTodoList(
    todos: List<TodoItem>,
    sortMode: SortMode,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    // 为每个有子任务的待办收集子任务数据
    val subTasksMap = remember { mutableStateMapOf<Long, List<SubTask>>() }

    // 收集子任务数据
    todos.filter { it.hasSubTasks }.forEach { todo ->
        val subTasks by viewModel.getSubTasks(todo.id).collectAsState(initial = emptyList())
        LaunchedEffect(subTasks) {
            subTasksMap[todo.id] = subTasks
        }
    }

    when (sortMode) {
        SortMode.BY_TIME -> {
            val sortedTodos = remember(todos) {
                todos.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenBy { it.dueDateTime }
                )
            }

            TodoList(
                todos = sortedTodos,
                onTodoClick = onTodoClick,
                onToggleComplete = onToggleComplete,
                onDeleteTodo = onDeleteTodo,
                modifier = modifier,
                subTasksMap = subTasksMap.toMap(),
                onToggleSubTask = { subTask -> viewModel.toggleSubTask(subTask) }
            )
        }
        SortMode.BY_PRIORITY, SortMode.BY_TAG -> {
            val groupedTodos = remember(todos) {
                todos.groupBy { it.priority }
                    .toSortedMap(compareByDescending { it.ordinal })
            }

            TodoListGrouped(
                groupedTodos = groupedTodos,
                onTodoClick = onTodoClick,
                onToggleComplete = onToggleComplete,
                onDeleteTodo = onDeleteTodo,
                modifier = modifier,
                subTasksMap = subTasksMap.toMap(),
                onToggleSubTask = { subTask -> viewModel.toggleSubTask(subTask) }
            )
        }
    }
}
