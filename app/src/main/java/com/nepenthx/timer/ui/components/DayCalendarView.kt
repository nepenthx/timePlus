package com.nepenthx.timer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.ui.theme.LocalAppColors
import com.nepenthx.timer.utils.DateUtils
import java.time.LocalDate

@Composable
fun DayCalendarView(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    
    Column(modifier = modifier) {
        // 日期标题
        Text(
            text = DateUtils.formatDate(selectedDate),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = appColors.calendar,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        // 星期
        Text(
            text = "星期${getDayOfWeekChinese(selectedDate)}",
            style = MaterialTheme.typography.titleMedium,
            color = appColors.date.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
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
