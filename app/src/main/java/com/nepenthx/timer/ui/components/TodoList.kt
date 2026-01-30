package com.nepenthx.timer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.theme.LocalAppColors

@Composable
fun TodoList(
    todos: List<TodoItem>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    
    if (todos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📝",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "暂无待办事项",
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.text.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(todos, key = { it.id }) { todo ->
                TodoItemCard(
                    todo = todo,
                    onClick = { onTodoClick(todo) },
                    onToggleComplete = { onToggleComplete(todo) },
                    onDelete = { onDeleteTodo(todo) }
                )
            }
            
            // 底部留空，避免被FAB遮挡
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun TodoListGrouped(
    groupedTodos: Map<Priority, List<TodoItem>>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    
    if (groupedTodos.isEmpty() || groupedTodos.values.all { it.isEmpty() }) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📝",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "暂无待办事项",
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.text.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedTodos.forEach { (priority, todoList) ->
                if (todoList.isNotEmpty()) {
                    item {
                        PriorityGroupHeader(priority = priority)
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
                    
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                }
            }
            
            // 底部留空
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun PriorityGroupHeader(priority: Priority) {
    val (color, text, icon) = when (priority) {
        Priority.HIGH -> Triple(Color(0xFFEF5350), "高优先级", "🔴")
        Priority.MEDIUM -> Triple(Color(0xFFFFA726), "中优先级", "🟡")
        Priority.LOW -> Triple(Color(0xFF66BB6A), "低优先级", "🟢")
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
