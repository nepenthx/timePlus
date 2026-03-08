package com.nepenthx.timer.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.theme.LocalAppColors
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    allTodos: List<TodoItem>,
    allTags: List<TodoTag>,
    subTasksProvider: @Composable (Long) -> List<SubTask>,
    onTodoClick: (TodoItem) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { animateIn = true }

    BackHandler { onDismiss() }

    val subTasksCache = remember { mutableMapOf<Long, List<SubTask>>() }

    allTodos.filter { it.hasSubTasks }.forEach { todo ->
        val subTasks = subTasksProvider(todo.id)
        LaunchedEffect(subTasks) {
            subTasksCache[todo.id] = subTasks
        }
    }

    val searchResults = remember(searchQuery, allTodos, subTasksCache.toMap()) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val query = searchQuery.lowercase()
            allTodos.filter { todo ->
                todo.title.lowercase().contains(query) ||
                        todo.note.lowercase().contains(query) ||
                        (todo.hasSubTasks && subTasksCache[todo.id]?.any { it.title.lowercase().contains(query) } == true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = animateIn,
            enter = slideInVertically(animationSpec = tween(350)) { -it / 4 } + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(animationSpec = tween(250)) { -it / 4 } + fadeOut(animationSpec = tween(200))
        ) {
            Scaffold(
                containerColor = appColors.background,
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = appColors.background,
                            navigationIconContentColor = appColors.text
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索标题、备注、子任务…") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = appColors.primary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.primary,
                            unfocusedBorderColor = appColors.text.copy(alpha = 0.2f),
                            focusedContainerColor = appColors.card.copy(alpha = 0.3f),
                            unfocusedContainerColor = appColors.card.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .focusRequester(focusRequester)
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (searchQuery.isBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = appColors.text.copy(alpha = 0.2f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "输入关键词搜索任务",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = appColors.text.copy(alpha = 0.4f)
                                )
                            }
                        }
                    } else if (searchResults.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "没有找到匹配的任务",
                                style = MaterialTheme.typography.bodyLarge,
                                color = appColors.text.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        Text(
                            text = "找到 ${searchResults.size} 个结果",
                            style = MaterialTheme.typography.labelMedium,
                            color = appColors.text.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults, key = { it.id }) { todo ->
                                val tag = allTags.firstOrNull { it.id == todo.tagId }
                                val matchedSubTask = subTasksCache[todo.id]
                                    ?.firstOrNull { it.title.lowercase().contains(searchQuery.lowercase()) }

                                SearchResultCard(
                                    todo = todo,
                                    query = searchQuery,
                                    tag = tag,
                                    matchedSubTask = matchedSubTask,
                                    onClick = {
                                        onTodoClick(todo)
                                        onDismiss()
                                    }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    todo: TodoItem,
    query: String,
    tag: TodoTag?,
    matchedSubTask: SubTask?,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = appColors.card.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = appColors.text,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (tag != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(tag.color))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tag.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(tag.color)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = todo.dueDateTime.format(DateTimeFormatter.ofPattern("M月d日 HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.text.copy(alpha = 0.5f)
                )
                if (todo.isCompleted) {
                    Text(
                        text = "已完成",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.primary
                    )
                }
            }

            if (todo.note.isNotBlank() && todo.note.lowercase().contains(query.lowercase())) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "备注: ${todo.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.text.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (matchedSubTask != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "子任务: ${matchedSubTask.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.primary.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
