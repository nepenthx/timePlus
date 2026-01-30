package com.nepenthx.timer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.data.WeekDays
import com.nepenthx.timer.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    selectedDate: LocalDate,
    tags: List<TodoTag> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        note: String,
        priority: Priority,
        dateTime: LocalDateTime,
        recurringType: RecurringType,
        customWeekDays: Int,
        tagId: Long?,
        enableNotification: Boolean,
        notifyMinutesBefore: Int,
        hasSubTasks: Boolean
    ) -> Unit
) {
    val appColors = LocalAppColors.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var recurringType by remember { mutableStateOf(RecurringType.NONE) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var customWeekDays by remember { mutableStateOf(0) }
    var selectedTagId by remember { mutableStateOf<Long?>(null) }
    var enableNotification by remember { mutableStateOf(false) }
    var notifyMinutesBefore by remember { mutableStateOf(15) }
    var hasSubTasks by remember { mutableStateOf(false) }

    var showPriorityMenu by remember { mutableStateOf(false) }
    var showRecurringMenu by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showNotifyTimeMenu by remember { mutableStateOf(false) }

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
                    minLines = 2,
                    maxLines = 4
                )

                // 日期时间
                OutlinedButton(
                    onClick = { showTimePickerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${selectedDate} ${String.format("%02d:%02d", selectedTime.hour, selectedTime.minute)}")
                }

                // 优先级选择
                Box {
                    OutlinedButton(
                        onClick = { showPriorityMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null)
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
                        Icon(Icons.Default.Repeat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重复: ${getRecurringText(recurringType, customWeekDays)}")
                    }

                    DropdownMenu(
                        expanded = showRecurringMenu,
                        onDismissRequest = { showRecurringMenu = false }
                    ) {
                        RecurringType.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(getRecurringTypeLabel(r)) },
                                onClick = {
                                    recurringType = r
                                    if (r == RecurringType.CUSTOM_WEEKLY && customWeekDays == 0) {
                                        customWeekDays = WeekDays.fromDayOfWeek(selectedDate.dayOfWeek.value)
                                    }
                                    showRecurringMenu = false
                                }
                            )
                        }
                    }
                }

                // 自定义每周重复日期选择
                if (recurringType == RecurringType.CUSTOM_WEEKLY) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = appColors.card.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "选择重复日期",
                                style = MaterialTheme.typography.labelMedium,
                                color = appColors.text.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                WeekDays.ALL_DAYS.forEach { (flag, name) ->
                                    val isSelected = (customWeekDays and flag) != 0
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            customWeekDays = if (isSelected) {
                                                customWeekDays and flag.inv()
                                            } else {
                                                customWeekDays or flag
                                            }
                                        },
                                        label = { Text(name.takeLast(1)) },
                                        modifier = Modifier.size(44.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = appColors.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                            
                            if (customWeekDays != 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "已选: ${WeekDays.getSelectedDays(customWeekDays).joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.primary
                                )
                            }
                        }
                    }
                }

                // 标签选择
                if (tags.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = appColors.card.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "选择标签",
                                style = MaterialTheme.typography.labelMedium,
                                color = appColors.text.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedTagId == null,
                                        onClick = { selectedTagId = null },
                                        label = { Text("默认") },
                                        leadingIcon = if (selectedTagId == null) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                                items(tags) { tag ->
                                    FilterChip(
                                        selected = selectedTagId == tag.id,
                                        onClick = { selectedTagId = tag.id },
                                        label = { Text(tag.name) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(tag.color))
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(tag.color).copy(alpha = 0.2f),
                                            selectedLabelColor = Color(tag.color)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 通知设置
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = appColors.card.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = appColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "开启通知提醒",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked = enableNotification,
                                onCheckedChange = { enableNotification = it }
                            )
                        }

                        if (enableNotification) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box {
                                OutlinedButton(
                                    onClick = { showNotifyTimeMenu = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("提前 ${getNotifyTimeText(notifyMinutesBefore)} 提醒")
                                }
                                DropdownMenu(
                                    expanded = showNotifyTimeMenu,
                                    onDismissRequest = { showNotifyTimeMenu = false }
                                ) {
                                    listOf(5, 10, 15, 30, 60, 120, 1440).forEach { minutes ->
                                        DropdownMenuItem(
                                            text = { Text(getNotifyTimeText(minutes)) },
                                            onClick = {
                                                notifyMinutesBefore = minutes
                                                showNotifyTimeMenu = false
                                            },
                                            leadingIcon = {
                                                if (minutes == notifyMinutesBefore) {
                                                    Icon(Icons.Default.Check, contentDescription = null)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 子任务开关
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = appColors.card.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                tint = appColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "启用子任务",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = hasSubTasks,
                            onCheckedChange = { hasSubTasks = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                        val finalRecurringType = if (recurringType == RecurringType.CUSTOM_WEEKLY && customWeekDays == 0) {
                            RecurringType.NONE
                        } else {
                            recurringType
                        }
                        onConfirm(
                            title, 
                            note, 
                            priority, 
                            dateTime, 
                            finalRecurringType, 
                            customWeekDays,
                            selectedTagId,
                            enableNotification,
                            notifyMinutesBefore,
                            hasSubTasks
                        )
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

    // 滚动时间选择对话框
    if (showTimePickerDialog) {
        ScrollTimePickerDialog(
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
fun ScrollTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val appColors = LocalAppColors.current
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }
    
    val scope = rememberCoroutineScope()
    
    // 小时列表状态
    val hourListState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedHour
    )
    // 分钟列表状态
    val minuteListState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedMinute
    )
    
    // 监听滚动位置变化
    LaunchedEffect(hourListState.firstVisibleItemIndex) {
        val centerIndex = hourListState.firstVisibleItemIndex + 1
        if (centerIndex in 0..23) {
            selectedHour = centerIndex
        }
    }
    
    LaunchedEffect(minuteListState.firstVisibleItemIndex) {
        val centerIndex = minuteListState.firstVisibleItemIndex + 1
        if (centerIndex in 0..59) {
            selectedMinute = centerIndex
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 显示当前选择的时间
                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 小时选择器
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // 选中区域高亮
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(appColors.primary.copy(alpha = 0.1f))
                        )
                        
                        LazyColumn(
                            state = hourListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(vertical = 66.dp)
                        ) {
                            items(24) { hour ->
                                val isSelected = hour == selectedHour
                                Text(
                                    text = String.format("%02d", hour),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) appColors.primary else appColors.text.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clickable {
                                            selectedHour = hour
                                            scope.launch {
                                                hourListState.animateScrollToItem(maxOf(0, hour - 1))
                                            }
                                        },
                                    textAlign = TextAlign.Center,
                                    fontSize = if (isSelected) 28.sp else 20.sp
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = appColors.text
                    )
                    
                    // 分钟选择器
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(appColors.primary.copy(alpha = 0.1f))
                        )
                        
                        LazyColumn(
                            state = minuteListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(vertical = 66.dp)
                        ) {
                            items(60) { minute ->
                                val isSelected = minute == selectedMinute
                                Text(
                                    text = String.format("%02d", minute),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) appColors.primary else appColors.text.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clickable {
                                            selectedMinute = minute
                                            scope.launch {
                                                minuteListState.animateScrollToItem(maxOf(0, minute - 1))
                                            }
                                        },
                                    textAlign = TextAlign.Center,
                                    fontSize = if (isSelected) 28.sp else 20.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 快捷时间按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("09:00", "12:00", "18:00", "21:00").forEach { timeStr ->
                        val parts = timeStr.split(":")
                        val h = parts[0].toInt()
                        val m = parts[1].toInt()
                        TextButton(
                            onClick = {
                                selectedHour = h
                                selectedMinute = m
                                scope.launch {
                                    hourListState.animateScrollToItem(maxOf(0, h - 1))
                                    minuteListState.animateScrollToItem(maxOf(0, m - 1))
                                }
                            }
                        ) {
                            Text(timeStr, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(selectedHour, selectedMinute)) }) {
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

private fun getRecurringTypeLabel(recurringType: RecurringType): String {
    return when (recurringType) {
        RecurringType.NONE -> "不重复"
        RecurringType.DAILY -> "每天"
        RecurringType.WEEKLY -> "每周同一天"
        RecurringType.MONTHLY -> "每月同一天"
        RecurringType.CUSTOM_WEEKLY -> "自定义每周"
    }
}

private fun getRecurringText(recurringType: RecurringType, customWeekDays: Int): String {
    return when (recurringType) {
        RecurringType.NONE -> "不重复"
        RecurringType.DAILY -> "每天"
        RecurringType.WEEKLY -> "每周同一天"
        RecurringType.MONTHLY -> "每月同一天"
        RecurringType.CUSTOM_WEEKLY -> {
            if (customWeekDays == 0) {
                "自定义每周"
            } else {
                val days = WeekDays.getSelectedDays(customWeekDays)
                if (days.size <= 3) {
                    "每${days.joinToString("、")}"
                } else {
                    "每周${days.size}天"
                }
            }
        }
    }
}

private fun getNotifyTimeText(minutes: Int): String {
    return when {
        minutes < 60 -> "${minutes}分钟"
        minutes == 60 -> "1小时"
        minutes < 1440 -> "${minutes / 60}小时"
        minutes == 1440 -> "1天"
        else -> "${minutes / 1440}天"
    }
}
