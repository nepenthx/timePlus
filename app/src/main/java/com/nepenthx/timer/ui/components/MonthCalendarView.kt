package com.nepenthx.timer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.ui.theme.LocalAppColors
import com.nepenthx.timer.utils.DateUtils
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthCalendarView(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    todosMap: Map<LocalDate, List<TodoItem>> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val days = remember(yearMonth) { DateUtils.getMonthDays(yearMonth) }
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")

    Column(modifier = modifier) {
        // 月份标题
        Text(
            text = DateUtils.formatMonth(yearMonth),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = appColors.text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        // 星期标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = appColors.date.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 日期网格
        Column(modifier = Modifier.fillMaxWidth()) {
            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                val dateTodos = todosMap[date] ?: emptyList()
                                CalendarDayCell(
                                    date = date,
                                    isSelected = date == selectedDate,
                                    isToday = date == LocalDate.now(),
                                    totalTodos = dateTodos.size,
                                    completedTodos = dateTodos.count { it.isCompleted },
                                    onClick = { onDateSelected(date) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    totalTodos: Int,
    completedTodos: Int,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    val hasTodos = totalTodos > 0
    
    val backgroundColor = when {
        isSelected -> appColors.calendar
        isToday -> appColors.calendar.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        isToday -> appColors.calendar
        else -> appColors.date
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (hasTodos) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // 显示总数和完成数
                    Text(
                        text = "$completedTodos/$totalTodos",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else appColors.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
