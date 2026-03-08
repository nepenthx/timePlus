package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.components.EmptyStateView
import com.nepenthx.timer.ui.components.SwipeableTaskRow
import com.nepenthx.timer.ui.components.TodoItemRow
import com.nepenthx.timer.ui.theme.LocalAppColors
import com.nepenthx.timer.viewmodel.TodoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodoViewModel,
    onTodoClick: (TodoItem) -> Unit
) {
    val appColors = LocalAppColors.current
    val todos by viewModel.todosForToday.collectAsState(initial = emptyList())
    
    val subTasksMap = remember { mutableStateMapOf<Long, List<SubTask>>() }
    
    todos.filter { it.hasSubTasks }.forEach { todo ->
        val subTasks by viewModel.getSubTasks(todo.id).collectAsState(initial = emptyList())
        LaunchedEffect(subTasks) {
            subTasksMap[todo.id] = subTasks
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val today = LocalDate.now()
        val dateStr = today.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = dateStr,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.text
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (todos.isNotEmpty()) {
            val completedCount = todos.count { it.isCompleted }
            Text(
                text = "今天 · ${completedCount}/${todos.size} 已完成",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.text.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        if (todos.isEmpty()) {
            EmptyStateView(
                message = "今天没有任务，享受你的一天",
                icon = Icons.Outlined.Today,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(todos, key = { it.id }) { todo ->
                    SwipeableTaskRow(
                        onSwipeToStart = { viewModel.deleteTodo(todo) },
                        onSwipeToEnd = { 
                            viewModel.updateTodo(todo.copy(dueDateTime = todo.dueDateTime.plusDays(1))) 
                        },
                        modifier = Modifier.animateItem()
                    ) {
                        TodoItemRow(
                            todo = todo,
                            onClick = { onTodoClick(todo) },
                            onToggleComplete = { viewModel.toggleTodoCompletion(todo) },
                            subTasks = subTasksMap[todo.id] ?: emptyList(),
                            onToggleSubTask = { subTask -> viewModel.toggleSubTask(subTask) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
