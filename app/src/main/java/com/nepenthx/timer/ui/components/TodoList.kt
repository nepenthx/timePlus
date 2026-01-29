package com.nepenthx.timer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.TodoItem

@Composable
fun TodoList(
    todos: List<TodoItem>,
    onTodoClick: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (todos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "暂无待办事项",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todos, key = { it.id }) { todo ->
                TodoItemCard(
                    todo = todo,
                    onClick = { onTodoClick(todo) },
                    onToggleComplete = { onToggleComplete(todo) },
                    onDelete = { onDeleteTodo(todo) }
                )
            }
        }
    }
}
