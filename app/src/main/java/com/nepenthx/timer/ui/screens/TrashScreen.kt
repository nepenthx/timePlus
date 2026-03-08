package com.nepenthx.timer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.components.EmptyStateView
import com.nepenthx.timer.ui.theme.LocalAppColors
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TrashScreen(
    deletedTodos: List<TodoItem>,
    tags: List<TodoTag>,
    onRestore: (TodoItem) -> Unit,
    onPermanentlyDelete: (TodoItem) -> Unit,
    onEmptyTrash: () -> Unit
) {
    val appColors = LocalAppColors.current
    var showEmptyConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<TodoItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "垃圾箱",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = appColors.text
                )
                Text(
                    text = "已删除的任务将在 30 天后自动清除",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.text.copy(alpha = 0.5f)
                )
            }

            if (deletedTodos.isNotEmpty()) {
                TextButton(
                    onClick = { showEmptyConfirmDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("清空")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (deletedTodos.isEmpty()) {
            EmptyStateView(
                message = "垃圾箱是空的",
                icon = Icons.Outlined.DeleteSweep,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(deletedTodos, key = { it.id }) { todo ->
                    TrashItemCard(
                        todo = todo,
                        tag = tags.firstOrNull { it.id == todo.tagId },
                        onRestore = { onRestore(todo) },
                        onDelete = { showDeleteConfirmDialog = todo },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (showEmptyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("清空垃圾箱") },
            text = { Text("确定要永久删除垃圾箱中的所有 ${deletedTodos.size} 个任务吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEmptyTrash()
                        showEmptyConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("全部删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    showDeleteConfirmDialog?.let { todo ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("永久删除") },
            text = { Text("确定要永久删除「${todo.title}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPermanentlyDelete(todo)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("永久删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TrashItemCard(
    todo: TodoItem,
    tag: TodoTag?,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val daysRemaining = todo.deletedAt?.let {
        val expiry = it.plusDays(30)
        val now = LocalDateTime.now()
        maxOf(0, Duration.between(now, expiry).toDays().toInt())
    } ?: 30

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appColors.card.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = appColors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (tag != null) {
                            Text(
                                text = tag.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color(tag.color)
                            )
                        }
                        Text(
                            text = "剩余 $daysRemaining 天",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (daysRemaining <= 7)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            else
                                appColors.text.copy(alpha = 0.5f)
                        )
                        todo.deletedAt?.let {
                            Text(
                                text = "删除于 ${it.format(DateTimeFormatter.ofPattern("M月d日 HH:mm"))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.text.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onRestore) {
                        Icon(
                            Icons.Default.RestoreFromTrash,
                            contentDescription = "恢复",
                            tint = appColors.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = "永久删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
