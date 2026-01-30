package com.nepenthx.timer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.export.DataExportHelper
import com.nepenthx.timer.ui.components.AddTodoDialog
import com.nepenthx.timer.ui.components.TodoDetailDialog
import com.nepenthx.timer.ui.theme.LocalAppColors
import com.nepenthx.timer.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Calendar : NavigationItem(
        route = "calendar",
        title = "日历",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )
    
    object AllTodos : NavigationItem(
        route = "all_todos",
        title = "待办",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List
    )
    
    object Profile : NavigationItem(
        route = "profile",
        title = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: TodoViewModel = viewModel()
) {
    val appColors = LocalAppColors.current
    val themeSettings by viewModel.themeSettings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedItem by remember { mutableStateOf(0) }
    val navigationItems = listOf(
        NavigationItem.Calendar,
        NavigationItem.AllTodos,
        NavigationItem.Profile
    )
    
    // 状态
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todosForSelectedDate by viewModel.todosForSelectedDate.collectAsState(initial = emptyList())
    val allTodos by viewModel.allTodos.collectAsState(initial = emptyList())
    val allTags by viewModel.allTags.collectAsState(initial = emptyList())
    val sortMode by viewModel.sortMode.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTodo by remember { mutableStateOf<TodoItem?>(null) }
    
    // 导入导出状态
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val content = DataExportHelper.readFromUri(context, it)
            if (content != null) {
                val isJson = content.trimStart().startsWith("{")
                if (isJson) {
                    viewModel.importFromJson(content)
                } else {
                    viewModel.importFromICal(content)
                }
                scope.launch {
                    snackbarHostState.showSnackbar("导入成功")
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("导入失败：无法读取文件")
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (themeSettings.gradientEnabled) {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                appColors.gradientStart,
                                appColors.gradientEnd
                            )
                        )
                    )
                } else {
                    Modifier.background(appColors.background)
                }
            ),
        containerColor = if (themeSettings.gradientEnabled) {
            Color.Transparent
        } else {
            appColors.background
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = appColors.surface.copy(alpha = 0.95f)
            ) {
                navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedItem == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = appColors.primary,
                            selectedTextColor = appColors.primary,
                            indicatorColor = appColors.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedItem != 2) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = appColors.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加待办", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItem) {
                0 -> CalendarScreen(
                    viewModel = viewModel,
                    onTodoClick = { todo ->
                        selectedTodo = todo
                        showDetailDialog = true
                    }
                )
                1 -> AllTodosScreen(
                    todos = allTodos,
                    tags = allTags,
                    sortMode = sortMode,
                    onSortModeChange = { viewModel.setSortMode(it) },
                    onTodoClick = { todo ->
                        selectedTodo = todo
                        showDetailDialog = true
                    },
                    onToggleComplete = { todo ->
                        viewModel.toggleTodoCompletion(todo)
                    },
                    onDeleteTodo = { todo ->
                        viewModel.deleteTodo(todo)
                    }
                )
                2 -> ProfileScreen(
                    themeSettings = themeSettings,
                    tags = allTags,
                    onPresetSelected = { preset ->
                        viewModel.setThemePreset(preset)
                    },
                    onCustomColorChange = { primary, secondary, background, surface, card, text, calendar, date, gradient, gradientStart, gradientEnd ->
                        viewModel.setCustomColor(
                            primaryColor = primary,
                            secondaryColor = secondary,
                            backgroundColor = background,
                            surfaceColor = surface,
                            cardColor = card,
                            textColor = text,
                            calendarColor = calendar,
                            dateColor = date,
                            gradientEnabled = gradient,
                            gradientStartColor = gradientStart,
                            gradientEndColor = gradientEnd
                        )
                    },
                    onAddTag = { name, color -> viewModel.addTag(name, color) },
                    onUpdateTag = { tag -> viewModel.updateTag(tag) },
                    onDeleteTag = { tag -> viewModel.deleteTag(tag) },
                    onExportICal = {
                        scope.launch {
                            try {
                                val content = viewModel.exportToICal()
                                val fileName = "todos_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.ics"
                                DataExportHelper.saveAndShareFile(context, content, fileName, "text/calendar")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("导出失败: ${e.message}")
                            }
                        }
                    },
                    onExportJson = {
                        scope.launch {
                            try {
                                val content = viewModel.exportToJson()
                                val fileName = "todos_backup_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.json"
                                DataExportHelper.saveAndShareFile(context, content, fileName, "application/json")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("备份失败: ${e.message}")
                            }
                        }
                    },
                    onImportData = {
                        filePickerLauncher.launch(arrayOf("text/calendar", "application/json", "*/*"))
                    }
                )
            }
        }
    }

    // 添加待办对话框
    if (showAddDialog) {
        AddTodoDialog(
            selectedDate = selectedDate,
            tags = allTags,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, note, priority, dateTime, recurringType, customWeekDays, tagId, enableNotification, notifyMinutesBefore, hasSubTasks ->
                viewModel.addTodo(
                    title = title,
                    note = note,
                    priority = priority,
                    dueDateTime = dateTime,
                    recurringType = recurringType,
                    customWeekDays = customWeekDays,
                    tagId = tagId,
                    enableNotification = enableNotification,
                    notifyMinutesBefore = notifyMinutesBefore,
                    hasSubTasks = hasSubTasks
                )
                showAddDialog = false
            }
        )
    }

    // 待办详情对话框
    if (showDetailDialog && selectedTodo != null) {
        val checkIns by viewModel.getCheckInsByTodoId(selectedTodo!!.id).collectAsState(initial = emptyList())
        val subTasks by viewModel.getSubTasks(selectedTodo!!.id).collectAsState(initial = emptyList())
        
        TodoDetailDialog(
            todo = selectedTodo!!,
            checkIns = checkIns,
            subTasks = subTasks,
            onDismiss = { 
                showDetailDialog = false
                selectedTodo = null
            },
            onComplete = { todo: TodoItem ->
                viewModel.toggleTodoCompletion(todo)
            },
            onDeleteTodo = { todo: TodoItem ->
                viewModel.deleteTodo(todo)
                showDetailDialog = false
                selectedTodo = null
            },
            onCheckIn = { date: java.time.LocalDate ->
                viewModel.checkIn(selectedTodo!!.id, date)
            },
            onCancelCheckIn = { date: java.time.LocalDate ->
                viewModel.cancelCheckIn(selectedTodo!!.id, date)
            },
            onUpdateTodo = { updatedTodo ->
                viewModel.updateTodo(updatedTodo)
                selectedTodo = updatedTodo
            },
            onAddSubTask = { title ->
                viewModel.addSubTask(selectedTodo!!.id, title)
            },
            onToggleSubTask = { subTask ->
                viewModel.toggleSubTask(subTask)
            },
            onDeleteSubTask = { subTask ->
                viewModel.deleteSubTask(subTask)
            }
        )
    }
}
