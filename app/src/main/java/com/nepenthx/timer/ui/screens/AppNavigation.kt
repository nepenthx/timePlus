/**
 * 应用主导航界面
 *
 * 本文件定义了应用的主导航架构，包含底部导航栏和三个主要页面的切换。
 * 使用 Jetpack Compose 的 Scaffold 组件构建整体布局。
 *
 * 主要功能：
 * - 底部三 Tab 导航（日历、待办、我的）
 * - 浮动添加按钮（在日历和待办页面显示）
 * - 添加待办对话框
 * - 待办详情对话框
 * - 数据导入导出功能
 *
 * 导航项：
 * - 日历：日历视图 + 当日待办列表
 * - 待办：所有待办列表视图
 * - 我的：个人中心（主题设置、标签管理、数据管理）
 *
 * @author nepenthx
 * @since 1.0
 */
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

/**
 * 导航项密封类
 *
 * 定义底部导航栏的导航项，包含路由、标题、选中图标和未选中图标。
 *
 * @property route 路由标识
 * @property title 显示标题
 * @property selectedIcon 选中状态的图标
 * @property unselectedIcon 未选中状态的图标
 */
sealed class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    /** 日历页面导航项 */
    object Calendar : NavigationItem(
        route = "calendar",
        title = "日历",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )
    
    /** 待办列表页面导航项 */
    object AllTodos : NavigationItem(
        route = "all_todos",
        title = "待办",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List
    )
    
    /** 个人中心页面导航项 */
    object Profile : NavigationItem(
        route = "profile",
        title = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

/**
 * 应用主导航组件
 *
 * 应用的主入口界面，包含底部导航栏、浮动按钮和页面内容区域。
 * 负责管理页面切换、对话框显示和数据导入导出。
 *
 * @param viewModel 待办视图模型
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: TodoViewModel = viewModel()
) {
    // ==================== 状态收集 ====================
    val appColors = LocalAppColors.current
    val themeSettings by viewModel.themeSettings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    /** 当前选中的导航项索引 */
    var selectedItem by remember { mutableStateOf(0) }
    val navigationItems = listOf(
        NavigationItem.Calendar,
        NavigationItem.AllTodos,
        NavigationItem.Profile
    )
    
    // 从 ViewModel 收集状态
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todosForSelectedDate by viewModel.todosForSelectedDate.collectAsState(initial = emptyList())
    val allTodos by viewModel.allTodos.collectAsState(initial = emptyList())
    val allTags by viewModel.allTags.collectAsState(initial = emptyList())
    val sortMode by viewModel.sortMode.collectAsState()
    
    // 对话框状态
    var showAddDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTodo by remember { mutableStateOf<TodoItem?>(null) }
    
    // Snackbar 状态，用于显示提示信息
    val snackbarHostState = remember { SnackbarHostState() }
    
    // ==================== 文件选择器 ====================
    /** 文件选择器，用于导入数据 */
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

    // ==================== 主界面布局 ====================
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
                    },
                    viewModel = viewModel
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

    // ==================== 添加待办对话框 ====================
    /** 显示添加待办对话框 */
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

    // ==================== 待办详情对话框 ====================
    /** 显示待办详情对话框 */
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
