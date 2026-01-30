package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.SortMode
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.components.TodoItemCard
import com.nepenthx.timer.ui.theme.LocalAppColors

// 筛选模式
enum class FilterMode(val displayName: String) {
    ALL("全部"),
    NON_RECURRING("排除重复"),
    RECURRING_ONLY("仅重复")
}

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
    modifier: Modifier = Modifier
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

    Column(modifier = modifier.fillMaxSize()) {
        // 顶部标题和排序
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "所有待办",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.text
            )
            
            Box {
                FilledTonalButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(sortMode.displayName, style = MaterialTheme.typography.labelMedium)
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.displayName) },
                            onClick = {
                                onSortModeChange(mode)
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
                            }
                        ) 
                    },
                    leadingIcon = if (filterMode == mode) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = appColors.primary.copy(alpha = 0.2f),
                        selectedLabelColor = appColors.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 待办统计
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.card.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    onDeleteTodo = onDeleteTodo
                )
            }
            SortMode.BY_PRIORITY -> {
                TodoListByPriority(
                    todos = filteredTodos,
                    onTodoClick = onTodoClick,
                    onToggleComplete = onToggleComplete,
                    onDeleteTodo = onDeleteTodo
                )
            }
            SortMode.BY_TAG -> {
                TodoListByTag(
                    todos = filteredTodos,
                    tags = tags,
                    onTodoClick = onTodoClick,
                    onToggleComplete = onToggleComplete,
                    onDeleteTodo = onDeleteTodo
                )
            }
        }
    }
}

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

@Composable
private fun TodoListByTime(
    todos: List<TodoItem>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit
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
                onDelete = { onDeleteTodo(todo) }
            )
        }
    }
}

@Composable
private fun TodoListByPriority(
    todos: List<TodoItem>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit
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
                    onDelete = { onDeleteTodo(todo) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

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

@Composable
private fun TodoListByTag(
    todos: List<TodoItem>,
    tags: List<TodoTag>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit
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
                    onDelete = { onDeleteTodo(todo) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

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
