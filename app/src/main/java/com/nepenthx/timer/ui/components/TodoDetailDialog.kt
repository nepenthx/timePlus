package com.nepenthx.timer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.CheckInRecord
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.TodoItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailDialog(
    todo: TodoItem,
    checkIns: List<CheckInRecord>,
    onDismiss: () -> Unit,
    onCheckIn: (LocalDate) -> Unit,
    onCancelCheckIn: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val isCheckedInToday = checkIns.any { it.checkInDate == today }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(todo.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 时间信息
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = todo.dueDateTime.format(
                                    DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
                                )
                            )
                        }
                    }
                }

                // 优先级和周期性
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityChip(priority = todo.priority)
                    if (todo.recurringType != RecurringType.NONE) {
                        RecurringChip(recurringType = todo.recurringType)
                    }
                }

                // 备注
                if (todo.note.isNotBlank()) {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "备注",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = todo.note)
                        }
                    }
                }

                // 周期性待办的打卡功能
                if (todo.recurringType != RecurringType.NONE) {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "打卡记录",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "已打卡 ${checkIns.size} 天",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // 打卡按钮
                                if (isCheckedInToday) {
                                    FilledTonalButton(
                                        onClick = { onCancelCheckIn(today) }
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("已打卡")
                                    }
                                } else {
                                    Button(
                                        onClick = { onCheckIn(today) }
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("打卡")
                                    }
                                }
                            }

                            // 最近的打卡记录
                            if (checkIns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "最近打卡",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                checkIns.take(5).forEach { checkIn ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = checkIn.checkInDate.format(
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                            ),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 完成状态
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (todo.isCompleted) "已完成" else "未完成",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = if (todo.isCompleted) Icons.Default.CheckCircle
                            else Icons.Default.Search,
                            contentDescription = null,
                            tint = if (todo.isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
