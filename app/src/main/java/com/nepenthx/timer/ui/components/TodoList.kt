/**
 * 待办列表组件
 *
 * 本文件定义了待办列表的显示组件，支持普通列表和分组列表两种显示模式。
 *
 * 主要组件：
 * - TodoList: 普通待办列表，按顺序显示所有待办
 * - TodoListGrouped: 分组待办列表，按优先级分组显示
 * - PriorityGroupHeader: 优先级分组标题组件
 *
 * @author nepenthx
 * @since 1.0
 */
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
import androidx.compose.ui.unit.sp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.theme.LocalAppColors

@Composable
fun TodoList(
    todos: List<TodoItem>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    modifier: Modifier = Modifier,
    subTasksMap: Map<Long, List<SubTask>> = emptyMap(),
    onToggleSubTask: ((SubTask) -> Unit)? = null
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
                    fontSize = 14.sp,
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
            items(todos, key = { it.id }) { todo ->
                TodoItemCard(
                    todo = todo,
                    onClick = { onTodoClick(todo) },
                    onToggleComplete = { onToggleComplete(todo) },
                    onDelete = { onDeleteTodo(todo) },
                    subTasks = subTasksMap[todo.id] ?: emptyList(),
                    onToggleSubTask = onToggleSubTask
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
    modifier: Modifier = Modifier,
    subTasksMap: Map<Long, List<SubTask>> = emptyMap(),
    onToggleSubTask: ((SubTask) -> Unit)? = null
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
                    fontSize = 14.sp,
                    color = appColors.text.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            onDelete = { onDeleteTodo(todo) },
                            subTasks = subTasksMap[todo.id] ?: emptyList(),
                            onToggleSubTask = onToggleSubTask
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
