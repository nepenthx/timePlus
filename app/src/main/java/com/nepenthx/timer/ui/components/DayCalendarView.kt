package com.nepenthx.timer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.utils.DateUtils
import java.time.LocalDate

@Composable
fun DayCalendarView(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 日期标题
        Text(
            text = DateUtils.formatDate(selectedDate),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        // 星期
        Text(
            text = "星期${getDayOfWeekChinese(selectedDate)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        HorizontalDivider()

        // 待办列表将在下方显示
    }
}

fun getDayOfWeekChinese(date: LocalDate): String {
    return when (date.dayOfWeek.value) {
        1 -> "一"
        2 -> "二"
        3 -> "三"
        4 -> "四"
        5 -> "五"
        6 -> "六"
        7 -> "日"
        else -> ""
    }
}
