package com.nepenthx.timer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun WeekCalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    todosMap: Map<LocalDate, List<TodoItem>> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val weekDays = remember(selectedDate) { DateUtils.getWeekDays(selectedDate) }
    val weekDayNames = listOf("日", "一", "二", "三", "四", "五", "六")

    Column(modifier = modifier) {
        // 周标题
        val weekRange = "${DateUtils.formatDate(weekDays.first(), "M月d日")} - ${DateUtils.formatDate(weekDays.last(), "M月d日")}"
        Text(
            text = weekRange,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = appColors.text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )

        // 周视图 - 横向显示一周的日期，增加间距
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
                    weekDays.forEachIndexed { index, date ->
                        val dateTodos = todosMap[date] ?: emptyList()
                        WeekDayCell(
                            date = date,
                            dayName = weekDayNames[index],
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            totalTodos = dateTodos.size,
                            completedTodos = dateTodos.count { it.isCompleted },
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        )
                    }
        }
    }
}

@Composable
fun WeekDayCell(
    date: LocalDate,
    dayName: String,
    isSelected: Boolean,
    isToday: Boolean,
    totalTodos: Int,
    completedTodos: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when {
            isSelected -> appColors.calendar
            isToday -> appColors.calendar.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 星期名
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isSelected -> Color.White
                    isToday -> appColors.calendar
                    else -> appColors.date.copy(alpha = 0.6f)
                }
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // 日期数字
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> appColors.calendar
                    else -> appColors.date
                }
            )
            
            // 待办统计
            Spacer(modifier = Modifier.height(6.dp))
            if (totalTodos > 0) {
                Text(
                    text = "$completedTodos/$totalTodos",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else appColors.primary.copy(alpha = 0.8f)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
