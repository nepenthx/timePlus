package com.nepenthx.timer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@Composable
fun TodoItemRow(
    todo: TodoItem,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    subTasks: List<SubTask> = emptyList(),
    onToggleSubTask: ((SubTask) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    // 本地动画状态：仅用于驱动勾选+划线的视觉效果
    // 当用户点击勾选时，先在 UI 上展示动画，延迟后再真正更新数据
    var pendingComplete by remember { mutableStateOf(false) }

    // 视觉上是否显示为"已完成"
    val visuallyCompleted = todo.isCompleted || pendingComplete

    // 删除线动画进度
    val strikethroughProgress by animateFloatAsState(
        targetValue = if (visuallyCompleted) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "StrikethroughProgress"
    )

    // 延迟触发真实数据更新
    LaunchedEffect(pendingComplete) {
        if (pendingComplete) {
            delay(800) // 等待勾选+划线动画展示完毕
            onToggleComplete()
            pendingComplete = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedCheckbox(
                checked = visuallyCompleted,
                onCheckedChange = {
                    if (!todo.isCompleted && !pendingComplete) {
                        pendingComplete = true
                    } else if (todo.isCompleted) {
                        // 取消完成：立即执行
                        onToggleComplete()
                    }
                },
                priority = todo.priority
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (visuallyCompleted) appColors.text.copy(alpha = 0.5f) else appColors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.drawWithContent {
                        drawContent()
                        if (strikethroughProgress > 0f) {
                            val strokeWidth = 2.dp.toPx()
                            val y = size.height / 2
                            drawLine(
                                color = appColors.text.copy(alpha = 0.5f),
                                start = Offset(0f, y),
                                end = Offset(size.width * strikethroughProgress, y),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = todo.dueDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (todo.dueDateTime.toLocalDate().isBefore(java.time.LocalDate.now()) && !visuallyCompleted)
                            MaterialTheme.colorScheme.error
                        else
                            appColors.text.copy(alpha = 0.5f)
                    )

                    if (todo.recurringType != RecurringType.NONE) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = appColors.text.copy(alpha = 0.5f)
                        )
                    }

                    if (todo.hasSubTasks) {
                        val completedCount = subTasks.count { it.isCompleted }
                        Text(
                            text = "$completedCount/${subTasks.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.text.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            PriorityDot(priority = todo.priority)
        }

        HorizontalDivider(
            modifier = Modifier.padding(start = 52.dp),
            color = appColors.text.copy(alpha = 0.1f),
            thickness = 0.5.dp
        )
    }
}

@Composable
fun PriorityDot(priority: Priority) {
    val color = when (priority) {
        Priority.HIGH -> Color(0xFFEF5350)
        Priority.MEDIUM -> Color(0xFFFFA726)
        Priority.LOW -> Color(0xFF66BB6A)
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}
