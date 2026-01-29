package com.nepenthx.timer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.utils.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeekCalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    todosMap: Map<LocalDate, List<TodoItem>> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val weekDays = remember(selectedDate) { DateUtils.getWeekDays(selectedDate) }
    val weekDayNames = listOf("日", "一", "二", "三", "四", "五", "六")

    Column(modifier = modifier) {
        // 周标题
        val weekRange = "${DateUtils.formatDate(weekDays.first(), "M月d日")} - ${DateUtils.formatDate(weekDays.last(), "M月d日")}"
        Text(
            text = weekRange,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        // 周视图 - 横向显示一周的日期
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEachIndexed { index, date ->
                WeekDayCell(
                    date = date,
                    dayName = weekDayNames[index],
                    isSelected = date == selectedDate,
                    isToday = date == LocalDate.now(),
                    todoCount = todosMap[date]?.size ?: 0,
                    onClick = { onDateSelected(date) }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // 显示选中日期的待办列表
        Text(
            text = DateUtils.formatDate(selectedDate),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun WeekDayCell(
    date: LocalDate,
    dayName: String,
    isSelected: Boolean,
    isToday: Boolean,
    todoCount: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        modifier = Modifier.width(48.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            if (todoCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = todoCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
