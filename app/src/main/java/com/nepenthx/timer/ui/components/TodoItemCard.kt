/**
 * 待办卡片组件
 *
 * 本文件定义了待办事项卡片、优先级标签和周期性标签等UI组件。
 *
 * 主要组件：
 * - TodoItemCard: 待办事项卡片，显示标题、时间、备注和标签
 * - PriorityChip: 优先级标签组件
 * - RecurringChip: 周期性标签组件
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.theme.LocalAppColors
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItemCard(
    todo: TodoItem,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    subTasks: List<SubTask> = emptyList(),
    onToggleSubTask: ((SubTask) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = appColors.card.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成状态复选框
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 待办信息
            Column(modifier = Modifier.weight(1f)) {
                // 标题
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                    color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )

                // 时间
                Text(
                    text = todo.dueDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 备注
                if (todo.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                // 标签行（优先级和周期性）
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 优先级标签
                    PriorityChip(priority = todo.priority)

                    // 周期性标签
                    if (todo.recurringType != RecurringType.NONE) {
                        RecurringChip(
                            recurringType = todo.recurringType,
                            customWeekDays = todo.customWeekDays
                        )
                    }
                }
            }

            // 删除按钮
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个待办事项吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun PriorityChip(priority: Priority) {
    val (color, text) = when (priority) {
        Priority.HIGH -> Color(0xFFEF5350) to "高"
        Priority.MEDIUM -> Color(0xFFFFA726) to "中"
        Priority.LOW -> Color(0xFF66BB6A) to "低"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun RecurringChip(recurringType: RecurringType, customWeekDays: Int = 0) {
    val text = when (recurringType) {
        RecurringType.DAILY -> "每天"
        RecurringType.WEEKLY -> "每周"
        RecurringType.MONTHLY -> "每月"
        RecurringType.CUSTOM_WEEKLY -> {
            val days = com.nepenthx.timer.data.WeekDays.getSelectedDays(customWeekDays)
            if (days.size <= 2) {
                "每${days.joinToString("、")}"
            } else {
                "每周${days.size}天"
            }
        }
        RecurringType.NONE -> ""
    }

    if (text.isNotEmpty()) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
