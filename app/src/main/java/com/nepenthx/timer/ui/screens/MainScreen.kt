package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nepenthx.timer.ui.components.*
import com.nepenthx.timer.viewmodel.TodoViewModel
import com.nepenthx.timer.viewmodel.ViewMode
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TodoViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val todosForSelectedDate by viewModel.todosForSelectedDate.collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTodo by remember { mutableStateOf<com.nepenthx.timer.data.TodoItem?>(null) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("待办日历") },
                actions = {
                    // 视图模式切换按钮
                    IconButton(onClick = {
                        viewModel.setViewMode(
                            when (viewMode) {
                                ViewMode.MONTH -> ViewMode.WEEK
                                ViewMode.WEEK -> ViewMode.DAY
                                ViewMode.DAY -> ViewMode.MONTH
                            }
                        )
                    }) {
                        Icon(
                            imageVector = when (viewMode) {
                                ViewMode.MONTH -> Icons.Default.DateRange
                                ViewMode.WEEK -> Icons.Default.List
                                ViewMode.DAY -> Icons.Default.Info
                            },
                            contentDescription = "切换视图"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加待办")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 日历视图
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (viewMode) {
                    ViewMode.MONTH -> {
                        Column {
                            // 月份导航
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = {
                                    currentYearMonth = currentYearMonth.minusMonths(1)
                                }) {
                                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "上个月")
                                }
                                
                                TextButton(onClick = {
                                    currentYearMonth = YearMonth.now()
                                    viewModel.selectDate(LocalDate.now())
                                }) {
                                    Text("今天")
                                }
                                
                                IconButton(onClick = {
                                    currentYearMonth = currentYearMonth.plusMonths(1)
                                }) {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "下个月")
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
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    ViewMode.WEEK -> {
                        Column {
                            // 周导航
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = {
                                    viewModel.selectDate(selectedDate.minusDays(7))
                                }) {
                                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "上一周")
                                }
                                
                                TextButton(onClick = {
                                    viewModel.selectDate(LocalDate.now())
                                }) {
                                    Text("本周")
                                }
                                
                                IconButton(onClick = {
                                    viewModel.selectDate(selectedDate.plusDays(7))
                                }) {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "下一周")
                                }
                            }
                            
                            WeekCalendarView(
                                selectedDate = selectedDate,
                                onDateSelected = { viewModel.selectDate(it) },
                                todosMap = weekTodosMap,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    ViewMode.DAY -> {
                        Column {
                            // 日导航
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = {
                                    viewModel.selectDate(selectedDate.minusDays(1))
                                }) {
                                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "前一天")
                                }
                                
                                TextButton(onClick = {
                                    viewModel.selectDate(LocalDate.now())
                                }) {
                                    Text("今天")
                                }
                                
                                IconButton(onClick = {
                                    viewModel.selectDate(selectedDate.plusDays(1))
                                }) {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "后一天")
                                }
                            }
                            
                            DayCalendarView(
                                selectedDate = selectedDate,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            // 待办列表
            Text(
                text = "待办事项",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TodoList(
                todos = todosForSelectedDate,
                onTodoClick = { todo ->
                    selectedTodo = todo
                    showDetailDialog = true
                },
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

    // 添加待办对话框
    if (showAddDialog) {
        AddTodoDialog(
            selectedDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, note, priority, dateTime, recurringType ->
                viewModel.addTodo(
                    title = title,
                    note = note,
                    priority = priority,
                    dueDateTime = dateTime,
                    recurringType = recurringType
                )
                showAddDialog = false
            }
        )
    }

    // 待办详情对话框
    if (showDetailDialog && selectedTodo != null) {
        val checkIns by viewModel.getCheckInsByTodoId(selectedTodo!!.id)
            .collectAsState(initial = emptyList())

        TodoDetailDialog(
            todo = selectedTodo!!,
            checkIns = checkIns,
            onDismiss = {
                showDetailDialog = false
                selectedTodo = null
            },
            onCheckIn = { date ->
                viewModel.checkIn(selectedTodo!!.id, date)
            },
            onCancelCheckIn = { date ->
                viewModel.cancelCheckIn(selectedTodo!!.id, date)
            }
        )
    }
}
