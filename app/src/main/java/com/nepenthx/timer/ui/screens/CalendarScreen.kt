package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.SortMode
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.components.*
import com.nepenthx.timer.ui.theme.LocalAppColors
import com.nepenthx.timer.viewmodel.TodoViewModel
import com.nepenthx.timer.viewmodel.ViewMode
import java.time.LocalDate
import java.time.YearMonth

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
    
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    // 获取月视图的待办数据
    val monthTodos by remember(currentYearMonth) {
        val start = currentYearMonth.atDay(1)
        val end = currentYearMonth.atEndOfMonth()
        viewModel.getTodosByDateRange(start, end)
    }.collectAsState(initial = emptyList())

    val monthTodosMap = remember(monthTodos) {
        monthTodos.groupBy { it.dueDateTime.toLocalDate() }
    }

    // 获取周视图的待办数据
    val weekStart = remember(selectedDate) {
        selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() % 7)
    }
    val weekEnd = remember(weekStart) { weekStart.plusDays(6) }
    val weekTodos by remember(weekStart) {
        viewModel.getTodosByDateRange(weekStart, weekEnd)
    }.collectAsState(initial = emptyList())

    val weekTodosMap = remember(weekTodos) {
        weekTodos.groupBy { it.dueDateTime.toLocalDate() }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "待办日历",
                style = MaterialTheme.typography.headlineMedium,
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (viewMode) {
                        ViewMode.MONTH -> "月"
                        ViewMode.WEEK -> "周"
                        ViewMode.DAY -> "日"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // 日历视图
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                currentYearMonth = currentYearMonth.minusMonths(1)
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "上个月",
                                    tint = appColors.calendar
                                )
                            }
                            
                            TextButton(onClick = {
                                currentYearMonth = YearMonth.now()
                                viewModel.selectDate(LocalDate.now())
                            }) {
                                Text("今天", color = appColors.calendar)
                            }
                            
                            IconButton(onClick = {
                                currentYearMonth = currentYearMonth.plusMonths(1)
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "下个月",
                                    tint = appColors.calendar
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                ViewMode.WEEK -> {
                    Column {
                        // 周导航
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                viewModel.selectDate(selectedDate.minusDays(7))
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "上一周",
                                    tint = appColors.calendar
                                )
                            }
                            
                            TextButton(onClick = {
                                viewModel.selectDate(LocalDate.now())
                            }) {
                                Text("本周", color = appColors.calendar)
                            }
                            
                            IconButton(onClick = {
                                viewModel.selectDate(selectedDate.plusDays(7))
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "下一周",
                                    tint = appColors.calendar
                                )
                            }
                        }
                        
                        WeekCalendarView(
                            selectedDate = selectedDate,
                            onDateSelected = { viewModel.selectDate(it) },
                            todosMap = weekTodosMap,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
                ViewMode.DAY -> {
                    Column {
                        // 日导航
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                viewModel.selectDate(selectedDate.minusDays(1))
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "前一天",
                                    tint = appColors.calendar
                                )
                            }
                            
                            TextButton(onClick = {
                                viewModel.selectDate(LocalDate.now())
                            }) {
                                Text("今天", color = appColors.calendar)
                            }
                            
                            IconButton(onClick = {
                                viewModel.selectDate(selectedDate.plusDays(1))
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "后一天",
                                    tint = appColors.calendar
                                )
                            }
                        }
                        
                        DayCalendarView(
                            selectedDate = selectedDate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = appColors.text
            )
            
            Text(
                text = "${todosForSelectedDate.count { !it.isCompleted }}项待完成",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.text.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 待办列表 - 按分组模式显示
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
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GroupedTodoList(
    todos: List<TodoItem>,
    sortMode: SortMode,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {
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
                modifier = modifier
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
                modifier = modifier
            )
        }
    }
}
