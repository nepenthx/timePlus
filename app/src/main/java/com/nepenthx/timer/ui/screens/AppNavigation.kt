package com.nepenthx.timer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.export.DataExportHelper
import com.nepenthx.timer.ui.components.QuickAddPanel
import com.nepenthx.timer.ui.components.SidebarContent
import com.nepenthx.timer.ui.components.SidebarDestination
import com.nepenthx.timer.ui.theme.LocalAppColors
import com.nepenthx.timer.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: TodoViewModel = viewModel()
) {
    val appColors = LocalAppColors.current
    val themeSettings by viewModel.themeSettings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var selectedDestination by remember { mutableStateOf<SidebarDestination>(SidebarDestination.Today) }
    
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todosForSelectedDate by viewModel.todosForSelectedDate.collectAsState(initial = emptyList())
    val allTodos by viewModel.allTodos.collectAsState(initial = emptyList())
    val allTags by viewModel.allTags.collectAsState(initial = emptyList())
    val sortMode by viewModel.sortMode.collectAsState()
    
    var showQuickAddPanel by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTodo by remember { mutableStateOf<TodoItem?>(null) }
    
    val fabRotation by animateFloatAsState(
        targetValue = if (showQuickAddPanel) 45f else 0f,
        label = "FabRotation"
    )
    
    val snackbarHostState = remember { SnackbarHostState() }
    
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarContent(
                selectedDestination = selectedDestination,
                onDestinationSelected = { destination ->
                    selectedDestination = destination
                    scope.launch { drawerState.close() }
                },
                tags = allTags,
                todayCount = 0,
                upcomingCount = 0,
                anytimeCount = allTodos.count { !it.isCompleted },
                completedCount = allTodos.count { it.isCompleted }
            )
        }
    ) {
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
            topBar = {
                TopAppBar(
                    title = { Text(selectedDestination.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = appColors.text,
                        navigationIconContentColor = appColors.primary
                    ),
                    actions = {
                        if (selectedDestination is SidebarDestination.Anytime) {
                            var showSortMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.Default.Sort, contentDescription = "排序", tint = appColors.primary)
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    com.nepenthx.timer.data.SortMode.entries.forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode.displayName) },
                                            onClick = {
                                                viewModel.setSortMode(mode)
                                                showSortMenu = false
                                            },
                                            leadingIcon = {
                                                if (mode == sortMode) {
                                                    Icon(Icons.Default.Check, contentDescription = null)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (selectedDestination !is SidebarDestination.Settings) {
                    FloatingActionButton(
                        onClick = { showQuickAddPanel = !showQuickAddPanel },
                        containerColor = appColors.primary
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加待办",
                            tint = Color.White,
                            modifier = Modifier.rotate(fabRotation)
                        )
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedDestination,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { width -> width / 10 } togetherWith
                            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { width -> -width / 10 }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "PageTransition"
            ) { destination ->
                when (destination) {
                    is SidebarDestination.Today -> {
                        TodayScreen(
                            viewModel = viewModel,
                            onTodoClick = { todo ->
                                selectedTodo = todo
                                showDetailDialog = true
                            }
                        )
                    }
                    is SidebarDestination.Upcoming -> {
                        UpcomingScreen(
                            viewModel = viewModel,
                            onTodoClick = { todo ->
                                selectedTodo = todo
                                showDetailDialog = true
                            }
                        )
                    }
                    is SidebarDestination.Anytime -> {
                        val filteredTodos = allTodos.filter { !it.isCompleted }
                        AllTodosScreen(
                            todos = filteredTodos,
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
                            },
                            viewModel = viewModel
                        )
                    }
                    is SidebarDestination.Completed -> {
                        val filteredTodos = allTodos.filter { it.isCompleted }
                        AllTodosScreen(
                            todos = filteredTodos,
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
                            },
                            viewModel = viewModel
                        )
                    }
                    is SidebarDestination.TagFilter -> {
                        val tagDestination = destination as SidebarDestination.TagFilter
                        val filteredTodos = allTodos.filter { it.tagId == tagDestination.tagId }
                        AllTodosScreen(
                            todos = filteredTodos,
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
                            },
                            viewModel = viewModel
                        )
                    }
                    is SidebarDestination.Settings -> {
                        SettingsScreen(
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
        }
    }

    // ==================== 快速添加面板 ====================
    if (showQuickAddPanel) {
        val initialDate = when (selectedDestination) {
            is SidebarDestination.Today -> LocalDate.now()
            is SidebarDestination.Upcoming -> LocalDate.now().plusDays(1)
            else -> LocalDate.now()
        }
        
        val initialTagId = if (selectedDestination is SidebarDestination.TagFilter) {
            (selectedDestination as SidebarDestination.TagFilter).tagId
        } else {
            null
        }

        QuickAddPanel(
            initialDate = initialDate,
            initialTagId = initialTagId,
            tags = allTags,
            onDismiss = { showQuickAddPanel = false },
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
            }
        )
    }

    // ==================== 待办详情对话框 ====================
    if (showDetailDialog && selectedTodo != null) {
        val checkIns by viewModel.getCheckInsByTodoId(selectedTodo!!.id).collectAsState(initial = emptyList())
        val subTasks by viewModel.getSubTasks(selectedTodo!!.id).collectAsState(initial = emptyList())
        
        TodoDetailScreen(
            todo = selectedTodo!!,
            checkIns = checkIns,
            subTasks = subTasks,
            onBack = { 
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
