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
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        note: String,
        priority: Priority,
        dateTime: LocalDateTime,
        recurringType: RecurringType
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var recurringType by remember { mutableStateOf(RecurringType.NONE) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }

    var showPriorityMenu by remember { mutableStateOf(false) }
    var showRecurringMenu by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加待办") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 备注输入
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // 日期时间
                OutlinedButton(
                    onClick = { showTimePickerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${selectedDate} ${selectedTime}")
                }

                // 优先级选择
                Box {
                    OutlinedButton(
                        onClick = { showPriorityMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("优先级: ${getPriorityText(priority)}")
                    }

                    DropdownMenu(
                        expanded = showPriorityMenu,
                        onDismissRequest = { showPriorityMenu = false }
                    ) {
                        Priority.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(getPriorityText(p)) },
                                onClick = {
                                    priority = p
                                    showPriorityMenu = false
                                }
                            )
                        }
                    }
                }

                // 周期性选择
                Box {
                    OutlinedButton(
                        onClick = { showRecurringMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重复: ${getRecurringText(recurringType)}")
                    }

                    DropdownMenu(
                        expanded = showRecurringMenu,
                        onDismissRequest = { showRecurringMenu = false }
                    ) {
                        RecurringType.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(getRecurringText(r)) },
                                onClick = {
                                    recurringType = r
                                    showRecurringMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                        onConfirm(title, note, priority, dateTime, recurringType)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // 时间选择对话框
    if (showTimePickerDialog) {
        TimePickerDialog(
            initialTime = selectedTime,
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { time ->
                selectedTime = time
                showTimePickerDialog = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    var hour by remember { mutableStateOf(initialTime.hour) }
    var minute by remember { mutableStateOf(initialTime.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 简单的小时分钟选择器
                Column {
                    Text("小时", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = hour.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { h ->
                                if (h in 0..23) hour = h
                            }
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("分钟", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = minute.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { m ->
                                if (m in 0..59) minute = m
                            }
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(hour, minute)) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun getPriorityText(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> "高"
        Priority.MEDIUM -> "中"
        Priority.LOW -> "低"
    }
}

private fun getRecurringText(recurringType: RecurringType): String {
    return when (recurringType) {
        RecurringType.NONE -> "不重复"
        RecurringType.DAILY -> "每天"
        RecurringType.WEEKLY -> "每周"
        RecurringType.MONTHLY -> "每月"
    }
}
