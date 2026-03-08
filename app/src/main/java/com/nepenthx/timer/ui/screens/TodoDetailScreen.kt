package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nepenthx.timer.data.CheckInRecord
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.SubTask
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.components.ScrollTimePickerDialog
import com.nepenthx.timer.ui.theme.LocalAppColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    todo: TodoItem,
    checkIns: List<CheckInRecord>,
    onBack: () -> Unit,
    onComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    onCheckIn: (LocalDate) -> Unit,
    onCancelCheckIn: (LocalDate) -> Unit,
    onUpdateTodo: (TodoItem) -> Unit,
    subTasks: List<SubTask> = emptyList(),
    onAddSubTask: (String) -> Unit = {},
    onToggleSubTask: (SubTask) -> Unit = {},
    onDeleteSubTask: (SubTask) -> Unit = {}
) {
    val appColors = LocalAppColors.current
    val today = LocalDate.now()
    val isCheckedInToday = checkIns.any { it.checkInDate == today }

    // 编辑状态
    var title by remember { mutableStateOf(todo.title) }
    var note by remember { mutableStateOf(todo.note) }
    var dueDateTime by remember { mutableStateOf(todo.dueDateTime) }
    var priority by remember { mutableStateOf(todo.priority) }
    
    var showTimePicker by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    
    // 子任务输入
    var newSubTaskTitle by remember { mutableStateOf("") }

    // 当离开页面时保存更改 (简单实现，实际可能需要更复杂的保存策略)
    DisposableEffect(Unit) {
        onDispose {
            if (title != todo.title || note != todo.note || dueDateTime != todo.dueDateTime || priority != todo.priority) {
                onUpdateTodo(todo.copy(title = title, note = note, dueDateTime = dueDateTime, priority = priority))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onDeleteTodo(todo) }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appColors.background
                )
            )
        },
        containerColor = appColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // 标题
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.text
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (title.isEmpty()) {
                Text("任务标题", color = appColors.text.copy(alpha = 0.3f), fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 属性行
            DetailRow(
                icon = Icons.Default.DateRange,
                label = dueDateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                onClick = { showTimePicker = true }
            )
            
            Box {
                DetailRow(
                    icon = Icons.Default.Flag,
                    label = when (priority) {
                        Priority.HIGH -> "高优先级"
                        Priority.MEDIUM -> "中优先级"
                        Priority.LOW -> "低优先级"
                    },
                    onClick = { showPriorityMenu = true },
                    iconTint = when (priority) {
                        Priority.HIGH -> Color(0xFFEF5350)
                        Priority.MEDIUM -> Color(0xFFFFA726)
                        Priority.LOW -> Color(0xFF66BB6A)
                    }
                )
                DropdownMenu(
                    expanded = showPriorityMenu,
                    onDismissRequest = { showPriorityMenu = false }
                ) {
                    Priority.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) }, // 简化显示
                            onClick = {
                                priority = p
                                showPriorityMenu = false
                            }
                        )
                    }
                }
            }

            // 标签和周期性暂略，逻辑类似

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = appColors.text.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // 子任务
            Text("子任务", style = MaterialTheme.typography.titleMedium, color = appColors.text)
            Spacer(modifier = Modifier.height(8.dp))
            
            subTasks.forEach { subTask ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = subTask.isCompleted,
                        onCheckedChange = { onToggleSubTask(subTask) }
                    )
                    Text(
                        text = subTask.title,
                        modifier = Modifier.weight(1f),
                        style = TextStyle(
                            textDecoration = if (subTask.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            color = appColors.text
                        )
                    )
                    IconButton(onClick = { onDeleteSubTask(subTask) }) {
                        Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            // 添加子任务
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = appColors.primary)
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = newSubTaskTitle,
                    onValueChange = { newSubTaskTitle = it },
                    textStyle = TextStyle(color = appColors.text),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (newSubTaskTitle.isEmpty()) {
                            Text("添加子任务", color = appColors.text.copy(alpha = 0.5f))
                        }
                        innerTextField()
                    }
                )
                if (newSubTaskTitle.isNotEmpty()) {
                    IconButton(onClick = {
                        onAddSubTask(newSubTaskTitle)
                        newSubTaskTitle = ""
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "添加")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = appColors.text.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // 备注
            Text("备注", style = MaterialTheme.typography.titleMedium, color = appColors.text)
            Spacer(modifier = Modifier.height(8.dp))
            BasicTextField(
                value = note,
                onValueChange = { note = it },
                textStyle = TextStyle(color = appColors.text, fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            if (note.isEmpty()) {
                Text("添加备注...", color = appColors.text.copy(alpha = 0.3f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 打卡区域 (如果是周期性任务)
            if (todo.recurringType != RecurringType.NONE) {
                HorizontalDivider(color = appColors.text.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("打卡记录", style = MaterialTheme.typography.titleMedium)
                        Text("已打卡 ${checkIns.size} 天", style = MaterialTheme.typography.bodySmall, color = appColors.text.copy(alpha = 0.6f))
                    }
                    Button(
                        onClick = {
                            if (isCheckedInToday) onCancelCheckIn(today) else onCheckIn(today)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCheckedInToday) appColors.secondary else appColors.primary
                        )
                    ) {
                        Text(if (isCheckedInToday) "已打卡" else "打卡")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showTimePicker) {
        ScrollTimePickerDialog(
            initialTime = dueDateTime.toLocalTime(),
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                dueDateTime = LocalDateTime.of(dueDateTime.toLocalDate(), time)
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color = LocalAppColors.current.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = LocalAppColors.current.text)
    }
}
