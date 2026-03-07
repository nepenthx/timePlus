/**
 * 所有待办屏幕
 *
 * 本文件定义了所有待办列表界面，显示用户的所有待办事项。
 * 提供筛选、排序和分组显示功能。
 *
 * 主要功能：
 * - 显示所有待办事项
 * - 筛选功能（全部/普通/重复）
 * - 排序功能（按时间/按优先级/按标签）
 * - 统计信息显示
 *
 * 筛选模式：
 * - ALL: 显示所有待办
 * - NON_RECURRING: 只显示非周期性待办
 * - RECURRING_ONLY: 只显示周期性待办
 *
 * 排序模式：
 * - BY_TIME: 按截止时间排序
 * - BY_PRIORITY: 按优先级分组
 * - BY_TAG: 按标签分组
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.SortMode
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.components.TodoItemCard
import com.nepenthx.timer.ui.theme.LocalAppColors

/**
 * 筛选模式枚举
 *
 * 定义待办的筛选模式。
 *
 * @property displayName 显示名称
 */
enum class FilterMode(val displayName: String) {
    ALL("全部"),
    NON_RECURRING("排除重复"),
    RECURRING_ONLY("仅重复")
}

/**
 * 所有待办屏幕组件
 *
 * 显示所有待办事项，支持筛选、排序和分组。
 *
 * @param todos 待办列表
 * @param tags 标签列表
 * @param sortMode 当前排序模式
 * @param onSortModeChange 排序模式变更回调
 * @param onTodoClick 待办点击回调
 * @param onToggleComplete 切换完成状态回调
 * @param onDeleteTodo 删除待办回调
 * @param modifier 修饰符
 * @param viewModel 视图模型
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTodosScreen(
    todos: List<TodoItem>,
    tags: List<TodoTag>,
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: com.nepenthx.timer.viewmodel.TodoViewModel? = null
) {
    val appColors = LocalAppColors.current
    var showSortMenu by remember { mutableStateOf(false) }
    var filterMode by remember { mutableStateOf(FilterMode.ALL) }

    // 根据筛选模式过滤待办
    val filteredTodos = remember(todos, filterMode) {
        when (filterMode) {
            FilterMode.ALL -> todos
            FilterMode.NON_RECURRING -> todos.filter { it.recurringType == RecurringType.NONE }
            FilterMode.RECURRING_ONLY -> todos.filter { it.recurringType != RecurringType.NONE }
        }
    }

    // 收集子任务数据
    val subTasksMap = remember { mutableStateMapOf<Long, List<SubTask>>() }
    viewModel?.let { vm ->
        filteredTodos.filter { it.hasSubTasks }.forEach { todo ->
            val subTasks by vm.getSubTasks(todo.id).collectAsState(initial = emptyList())
            LaunchedEffect(subTasks) {
                subTasksMap[todo.id] = subTasks
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 顶部标题和排序
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "所有待办",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.text
            )

            Box {
                FilledTonalButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(sortMode.displayName, fontSize = 12.sp)
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.displayName, fontSize = 13.sp) },
                            onClick = {
                                onSortModeChange(mode)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (mode == sortMode) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
            }
        }

        // 筛选选项卡
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FilterMode.entries) { mode ->
                FilterChip(
                    selected = filterMode == mode,
                    onClick = { filterMode = mode },
                    label = {
                        Text(
                            when (mode) {
                                FilterMode.ALL -> "全部 (${todos.size})"
                                FilterMode.NON_RECURRING -> "普通 (${todos.count { it.recurringType == RecurringType.NONE }})"
                                FilterMode.RECURRING_ONLY -> "重复 (${todos.count { it.recurringType != RecurringType.NONE }})"
                            },
                            fontSize = 11.sp
                        )
                    },
                    leadingIcon = if (filterMode == mode) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = appColors.primary.copy(alpha = 0.2f),
                        selectedLabelColor = appColors.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 待办统计
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.card.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("筛选结果", filteredTodos.size.toString(), appColors.primary)
                StatItem("待完成", filteredTodos.count { !it.isCompleted }.toString(), appColors.calendar)
                StatItem("已完成", filteredTodos.count { it.isCompleted }.toString(), appColors.secondary)
            }
        }

        // 分组显示的待办列表
        when (sortMode) {
            SortMode.BY_TIME -> {
                TodoListByTime(
                    todos = filteredTodos,
                    onTodoClick = onTodoClick,
                    onToggleComplete = onToggleComplete,
                    onDeleteTodo = onDeleteTodo,
                    subTasksMap = subTasksMap.toMap(),
                    onToggleSubTask = { subTask -> viewModel?.toggleSubTask(subTask) }
                )
            }
            SortMode.BY_PRIORITY -> {
                TodoListByPriority(
                    todos = filteredTodos,
                    onTodoClick = onTodoClick,
                    onToggleComplete = onToggleComplete,
                    onDeleteTodo = onDeleteTodo,
                    subTasksMap = subTasksMap.toMap(),
                    onToggleSubTask = { subTask -> viewModel?.toggleSubTask(subTask) }
                )
            }
            SortMode.BY_TAG -> {
                TodoListByTag(
                    todos = filteredTodos,
                    tags = tags,
                    onTodoClick = onTodoClick,
                    onToggleComplete = onToggleComplete,
                    onDeleteTodo = onDeleteTodo,
                    subTasksMap = subTasksMap.toMap(),
                    onToggleSubTask = { subTask -> viewModel?.toggleSubTask(subTask) }
                )
            }
        }
    }
}

/**
 * 统计项组件
 *
 * 显示一个统计数据的标签和数值。
 *
 * @param label 标签文字
 * @param value 数值文字
 * @param color 数值颜色
 */
@Composable
private fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LocalAppColors.current.text.copy(alpha = 0.6f)
        )
    }
}

/**
 * 按时间排序的待办列表组件
 *
 * 显示按截止时间排序的待办列表。
 *
 * @param todos 待办列表
 * @param onTodoClick 待办点击回调
 * @param onToggleComplete 切换完成状态回调
 * @param onDeleteTodo 删除待办回调
 * @param subTasksMap 子任务映射
 * @param onToggleSubTask 切换子任务状态回调
 */
@Composable
private fun TodoListByTime(
    todos: List<TodoItem>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    subTasksMap: Map<Long, List<SubTask>> = emptyMap(),
    onToggleSubTask: (SubTask) -> Unit = {}
) {
    val sortedTodos = remember(todos) {
        todos.sortedWith(
            compareBy<TodoItem> { it.isCompleted }
                .thenBy { it.dueDateTime }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortedTodos, key = { it.id }) { todo ->
            TodoItemCard(
                todo = todo,
                onClick = { onTodoClick(todo) },
                onToggleComplete = { onToggleComplete(todo) },
                onDelete = { onDeleteTodo(todo) },
                subTasks = subTasksMap[todo.id] ?: emptyList(),
                onToggleSubTask = onToggleSubTask
            )
        }
    }
}

/**
 * 按优先级分组的待办列表组件
 *
 * 显示按优先级分组的待办列表，每组有对应的优先级标题。
 *
 * @param todos 待办列表
 * @param onTodoClick 待办点击回调
 * @param onToggleComplete 切换完成状态回调
 * @param onDeleteTodo 删除待办回调
 * @param subTasksMap 子任务映射
 * @param onToggleSubTask 切换子任务状态回调
 */
@Composable
private fun TodoListByPriority(
    todos: List<TodoItem>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    subTasksMap: Map<Long, List<SubTask>> = emptyMap(),
    onToggleSubTask: (SubTask) -> Unit = {}
) {
    val appColors = LocalAppColors.current
    val groupedTodos = remember(todos) {
        todos.groupBy { it.priority }.toSortedMap(compareByDescending { it.ordinal })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedTodos.forEach { (priority, todoList) ->
            item {
                PriorityHeader(priority = priority)
            }

            items(
                todoList.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenBy { it.dueDateTime }
                ),
                key = { it.id }
            ) { todo ->
                TodoItemCard(
                    todo = todo,
                    onClick = { onTodoClick(todo) },
                    onToggleComplete = { onToggleComplete(todo) },
                    onDelete = { onDeleteTodo(todo) },
                    subTasks = subTasksMap[todo.id] ?: emptyList(),
                    onToggleSubTask = { onToggleSubTask(it) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

/**
 * 优先级标题组件
 *
 * 显示优先级分组的标题，包含对应颜色的标识条。
 *
 * @param priority 优先级
 */
@Composable
private fun PriorityHeader(priority: Priority) {
    val (color, text) = when (priority) {
        Priority.HIGH -> androidx.compose.ui.graphics.Color(0xFFEF5350) to "高优先级"
        Priority.MEDIUM -> androidx.compose.ui.graphics.Color(0xFFFFA726) to "中优先级"
        Priority.LOW -> androidx.compose.ui.graphics.Color(0xFF66BB6A) to "低优先级"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = color,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.size(4.dp, 16.dp)
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * 按标签分组的待办列表组件
 *
 * 显示按标签分组的待办列表，每组有对应的标签标题。
 *
 * @param todos 待办列表
 * @param tags 标签列表
 * @param onTodoClick 待办点击回调
 * @param onToggleComplete 切换完成状态回调
 * @param onDeleteTodo 删除待办回调
 * @param subTasksMap 子任务映射
 * @param onToggleSubTask 切换子任务状态回调
 */
@Composable
private fun TodoListByTag(
    todos: List<TodoItem>,
    tags: List<TodoTag>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    subTasksMap: Map<Long, List<SubTask>> = emptyMap(),
    onToggleSubTask: (SubTask) -> Unit = {}
) {
    val groupedTodos = remember(todos, tags) {
        val tagMap = tags.associateBy { it.id }
        todos.groupBy { todo ->
            todo.tagId?.let { tagMap[it]?.name } ?: "默认"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedTodos.forEach { (tagName, todoList) ->
            item {
                TagHeader(tagName = tagName)
            }

            items(
                todoList.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenByDescending { it.priority.ordinal }
                        .thenBy { it.dueDateTime }
                ),
                key = { it.id }
            ) { todo ->
                TodoItemCard(
                    todo = todo,
                    onClick = { onTodoClick(todo) },
                    onToggleComplete = { onToggleComplete(todo) },
                    onDelete = { onDeleteTodo(todo) },
                    subTasks = subTasksMap[todo.id] ?: emptyList(),
                    onToggleSubTask = { onToggleSubTask(it) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

/**
 * 标签标题组件
 *
 * 显示标签分组的标题。
 *
 * @param tagName 标签名称
 */
@Composable
private fun TagHeader(tagName: String) {
    val appColors = LocalAppColors.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Label,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = appColors.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = tagName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = appColors.text
        )
    }
}
