package com.nepenthx.timer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(
    viewModel: TodoViewModel,
    onTodoClick: (TodoItem) -> Unit
) {
    val appColors = LocalAppColors.current
    val todos by viewModel.upcomingTodos.collectAsState(initial = emptyList())
    
    val groupedTodos = remember(todos) {
        todos.groupBy { it.dueDateTime.toLocalDate() }
            .toSortedMap()
    }
    
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
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "即将到来",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.text
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (groupedTodos.isEmpty()) {
            EmptyStateView(
                message = "没有即将到来的任务",
                icon = Icons.Outlined.CalendarMonth,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                groupedTodos.forEach { (date, dailyTodos) ->
                    stickyHeader {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(appColors.background)
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = getRelativeDateString(date),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = appColors.text.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    
                    items(dailyTodos, key = { "${it.id}_${it.dueDateTime}" }) { todo ->
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
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

private fun getRelativeDateString(date: LocalDate): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)
    
    return when (date) {
        today -> "今天 · ${date.format(formatter)}"
        today.plusDays(1) -> "明天 · ${date.format(formatter)}"
        today.plusDays(2) -> "后天 · ${date.format(formatter)}"
        else -> date.format(formatter)
    }
}
