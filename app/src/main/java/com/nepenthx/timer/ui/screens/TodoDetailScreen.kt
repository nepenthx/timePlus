package com.nepenthx.timer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.components.ScrollDateTimePickerDialog
import com.nepenthx.timer.ui.components.TagSelector
import com.nepenthx.timer.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    todo: TodoItem,
    checkIns: List<CheckInRecord>,
    tags: List<TodoTag> = emptyList(),
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

    var title by remember { mutableStateOf(todo.title) }
    var note by remember { mutableStateOf(todo.note) }
    var dueDateTime by remember { mutableStateOf(todo.dueDateTime) }
    var priority by remember { mutableStateOf(todo.priority) }
    var tagId by remember { mutableStateOf(todo.tagId) }
    
    var showTimePicker by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    
    var newSubTaskTitle by remember { mutableStateOf("") }

    BackHandler {
        onBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (title != todo.title || note != todo.note || dueDateTime != todo.dueDateTime || priority != todo.priority || tagId != todo.tagId) {
                onUpdateTodo(todo.copy(title = title, note = note, dueDateTime = dueDateTime, priority = priority, tagId = tagId))
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
                            text = { Text(p.name) },
                            onClick = {
                                priority = p
                                showPriorityMenu = false
                            }
                        )
                    }
                }
            }

            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                TagSelector(
                    tags = tags,
                    selectedTagId = tagId,
                    onTagSelected = { newTagId ->
                        tagId = newTagId
                        onUpdateTodo(todo.copy(title = title, note = note, dueDateTime = dueDateTime, priority = priority, tagId = newTagId))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = appColors.text.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // 子任务
            Text("子任务", style = MaterialTheme.typography.titleMedium, color = appColors.text)
            Spacer(modifier = Modifier.height(8.dp))
            
            subTasks.forEach { subTask ->
                key(subTask.id) {
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    ) {
                        SubTaskRow(
                            subTask = subTask,
                            onToggle = { onToggleSubTask(subTask) },
                            onDelete = { onDeleteSubTask(subTask) }
                        )
                    }
                }
            }
            
            // 添加子任务（带动画）
            AnimatedVisibility(
                visible = true,
                enter = expandVertically() + fadeIn()
            ) {
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

            // 打卡区域
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
        ScrollDateTimePickerDialog(
            initialDateTime = dueDateTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { dateTime ->
                dueDateTime = dateTime
                showTimePicker = false
            }
        )
    }
}

/**
 * 子任务行组件，带勾选划线动画
 */
@Composable
private fun SubTaskRow(
    subTask: SubTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val appColors = LocalAppColors.current

    var pendingComplete by remember { mutableStateOf(false) }
    val visuallyCompleted = subTask.isCompleted || pendingComplete

    val strikethroughProgress by animateFloatAsState(
        targetValue = if (visuallyCompleted) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "SubTaskStrikethrough"
    )

    LaunchedEffect(pendingComplete) {
        if (pendingComplete) {
            delay(600)
            onToggle()
            pendingComplete = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = visuallyCompleted,
            onCheckedChange = {
                if (!subTask.isCompleted && !pendingComplete) {
                    pendingComplete = true
                } else if (subTask.isCompleted) {
                    onToggle()
                }
            }
        )
        Text(
            text = subTask.title,
            modifier = Modifier
                .weight(1f)
                .drawWithContent {
                    drawContent()
                    if (strikethroughProgress > 0f) {
                        val strokeWidth = 1.5.dp.toPx()
                        val y = size.height / 2
                        drawLine(
                            color = appColors.text.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(size.width * strikethroughProgress, y),
                            strokeWidth = strokeWidth
                        )
                    }
                },
            style = TextStyle(
                color = if (visuallyCompleted) appColors.text.copy(alpha = 0.5f) else appColors.text
            )
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.size(16.dp))
        }
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
