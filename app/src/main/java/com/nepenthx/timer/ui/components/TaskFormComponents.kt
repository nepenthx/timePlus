package com.nepenthx.timer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.data.WeekDays
import com.nepenthx.timer.ui.theme.LocalAppColors
import java.time.LocalDate

@Composable
fun PrioritySelector(
    priority: Priority,
    onPrioritySelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Flag, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("优先级: ${getPriorityText(priority)}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Priority.entries.forEach { p ->
                DropdownMenuItem(
                    text = { Text(getPriorityText(p)) },
                    onClick = {
                        onPrioritySelected(p)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RecurringSelector(
    recurringType: RecurringType,
    customWeekDays: Int,
    selectedDate: LocalDate,
    onRecurringTypeChange: (RecurringType) -> Unit,
    onCustomWeekDaysChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Repeat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重复: ${getRecurringText(recurringType, customWeekDays)}")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                RecurringType.entries.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(getRecurringTypeLabel(r)) },
                        onClick = {
                            onRecurringTypeChange(r)
                            if (r == RecurringType.CUSTOM_WEEKLY && customWeekDays == 0) {
                                onCustomWeekDaysChange(WeekDays.fromDayOfWeek(selectedDate.dayOfWeek.value))
                            }
                            expanded = false
                        }
                    )
                }
            }
        }

        // 自定义每周重复日期选择
        if (recurringType == RecurringType.CUSTOM_WEEKLY) {
            Spacer(modifier = Modifier.height(8.dp))
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
                                    val newDays = if (isSelected) {
                                        customWeekDays and flag.inv()
                                    } else {
                                        customWeekDays or flag
                                    }
                                    onCustomWeekDaysChange(newDays)
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
    }
}

@Composable
fun TagSelector(
    tags: List<TodoTag>,
    selectedTagId: Long?,
    onTagSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    
    if (tags.isNotEmpty()) {
        Card(
            modifier = modifier,
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
                            onClick = { onTagSelected(null) },
                            label = { Text("默认") },
                            leadingIcon = if (selectedTagId == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                    items(tags) { tag ->
                        FilterChip(
                            selected = selectedTagId == tag.id,
                            onClick = { onTagSelected(tag.id) },
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
}

@Composable
fun NotificationSelector(
    enableNotification: Boolean,
    notifyMinutesBefore: Int,
    onEnableChange: (Boolean) -> Unit,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier,
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
                    onCheckedChange = onEnableChange
                )
            }

            if (enableNotification) {
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("提前 ${getNotifyTimeText(notifyMinutesBefore)} 提醒")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        listOf(5, 10, 15, 30, 60, 120, 1440).forEach { minutes ->
                            DropdownMenuItem(
                                text = { Text(getNotifyTimeText(minutes)) },
                                onClick = {
                                    onMinutesChange(minutes)
                                    showMenu = false
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
}

// 辅助函数
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
