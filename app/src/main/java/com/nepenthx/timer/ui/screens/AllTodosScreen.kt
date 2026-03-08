package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.SortMode
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.components.EmptyStateView
import com.nepenthx.timer.ui.components.PullDownSearchLayout
import com.nepenthx.timer.ui.components.SwipeableTaskRow
import com.nepenthx.timer.ui.components.TodoItemRow
import com.nepenthx.timer.ui.theme.LocalAppColors
import com.nepenthx.timer.viewmodel.TodoViewModel

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
    viewModel: TodoViewModel? = null,
    onSearchTriggered: () -> Unit = {}
) {
    val appColors = LocalAppColors.current
    
    val sortedTodos = remember(todos, sortMode) {
        when (sortMode) {
            SortMode.BY_TIME -> todos.sortedWith(
                compareBy<TodoItem> { it.isCompleted }
                    .thenBy { it.dueDateTime }
            )
            SortMode.BY_PRIORITY -> todos.sortedWith(
                compareBy<TodoItem> { it.isCompleted }
                    .thenByDescending { it.priority.ordinal }
                    .thenBy { it.dueDateTime }
            )
            SortMode.BY_TAG -> todos.sortedWith(
                compareBy<TodoItem> { it.isCompleted }
                    .thenBy { it.tagId ?: -1 }
                    .thenBy { it.dueDateTime }
            )
        }
    }

    val subTasksMap = remember { mutableStateMapOf<Long, List<SubTask>>() }
    viewModel?.let { vm ->
        todos.filter { it.hasSubTasks }.forEach { todo ->
            val subTasks by vm.getSubTasks(todo.id).collectAsState(initial = emptyList())
            LaunchedEffect(subTasks) {
                subTasksMap[todo.id] = subTasks
            }
        }
    }

    PullDownSearchLayout(
        onSearchTriggered = onSearchTriggered,
        modifier = modifier
    ) {
        if (todos.isEmpty()) {
            EmptyStateView(
                message = "没有任务",
                icon = Icons.Outlined.Inbox,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sortedTodos, key = { it.id }) { todo ->
                    SwipeableTaskRow(
                        onSwipeToStart = { onDeleteTodo(todo) },
                        currentDateTime = todo.dueDateTime,
                        onPostpone = { newDateTime ->
                            viewModel?.updateTodo(todo.copy(dueDateTime = newDateTime))
                        },
                        modifier = Modifier.animateItem()
                    ) {
                        TodoItemRow(
                            todo = todo,
                            onClick = { onTodoClick(todo) },
                            onToggleComplete = { onToggleComplete(todo) },
                            subTasks = subTasksMap[todo.id] ?: emptyList(),
                            onToggleSubTask = { subTask -> viewModel?.toggleSubTask(subTask) }
                        )
                    }
                }
            }
        }
    }
}
