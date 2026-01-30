package com.nepenthx.timer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.CheckInRecord
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.theme.LocalAppColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailDialog(
    todo: TodoItem,
    checkIns: List<CheckInRecord>,
    onDismiss: () -> Unit,
    onComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    onCheckIn: (LocalDate) -> Unit,
    onCancelCheckIn: (LocalDate) -> Unit,
    onUpdateTodo: (TodoItem) -> Unit,
    subTasks: List<com.nepenthx.timer.data.SubTask> = emptyList(),
    onAddSubTask: (String) -> Unit = {},
    onToggleSubTask: (com.nepenthx.timer.data.SubTask) -> Unit = {},
    onDeleteSubTask: (com.nepenthx.timer.data.SubTask) -> Unit = {}
) {
    val today = LocalDate.now()
    val isCheckedInToday = checkIns.any { it.checkInDate == today }
    val appColors = LocalAppColors.current

    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(todo.title) }
    var editNote by remember { mutableStateOf(todo.note) }
    var editDateTime by remember { mutableStateOf(todo.dueDateTime) }
    var newSubTaskTitle by remember { mutableStateOf("") }
    
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            if (isEditing) {
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(todo.title, color = appColors.text)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 时间信息
                Card(
                    colors = CardDefaults.cardColors(containerColor = appColors.card.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = appColors.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isEditing) {
                                OutlinedButton(onClick = { showTimePicker = true }) {
                                    Text(editDateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")))
                                }
                            } else {
                                Text(
                                    text = todo.dueDateTime.format(
                                        DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
                                    )
                                )
                            }
                        }
                    }
                }

                if (showTimePicker) {
                    ScrollTimePickerDialog(
                        initialTime = editDateTime.toLocalTime(),
                        onDismiss = { showTimePicker = false },
                        onConfirm = { time ->
                            editDateTime = LocalDateTime.of(editDateTime.toLocalDate(), time)
                            showTimePicker = false
                        }
                    )
                }

                // 子任务部分
                if (todo.hasSubTasks) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = appColors.card.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "子任务",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            subTasks.forEach { subTask ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = subTask.isCompleted,
                                        onCheckedChange = { onToggleSubTask(subTask) }
                                    )
                                    Text(
                                        text = subTask.title,
                                        modifier = Modifier.weight(1f),
                                        style = if (subTask.isCompleted) MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(onClick = { onDeleteSubTask(subTask) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除子任务", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newSubTaskTitle,
                                    onValueChange = { newSubTaskTitle = it },
                                    label = { Text("添加子任务") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = {
                                        if (newSubTaskTitle.isNotBlank()) {
                                            onAddSubTask(newSubTaskTitle)
                                            newSubTaskTitle = ""
                                        }
                                    },
                                    enabled = newSubTaskTitle.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "添加")
                                }
                            }
                        }
                    }
                }

                // 备注
                if (isEditing || todo.note.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = appColors.card.copy(alpha = 0.7f))
                    ) {
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
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editNote,
                                    onValueChange = { editNote = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )
                            } else {
                                Text(text = todo.note)
                            }
                        }
                    }
                }
                // ... rest of the dialog content ...

                // 周期性待办的打卡功能
                if (todo.recurringType != RecurringType.NONE) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = appColors.card.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
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

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isEditing) {
                        Button(
                            onClick = {
                                onUpdateTodo(todo.copy(title = editTitle, note = editNote, dueDateTime = editDateTime))
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("保存")
                        }
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("编辑")
                        }
                        
                        OutlinedButton(
                            onClick = { onComplete(todo) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (todo.isCompleted) appColors.primary.copy(alpha = 0.1f) else Color.Transparent
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (todo.isCompleted) appColors.primary else appColors.text
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (todo.isCompleted) "已完成" else "标记完成")
                        }
                    }
                }
                
                if (!isEditing) {
                    OutlinedButton(
                        onClick = { onDeleteTodo(todo) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        dismissButton = null
    )
}
