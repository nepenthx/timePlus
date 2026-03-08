package com.nepenthx.timer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nepenthx.timer.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlin.math.abs

private const val ITEM_HEIGHT_DP = 44
private const val VISIBLE_ITEMS = 3

@Composable
fun ScrollDateTimePickerDialog(
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit
) {
    val appColors = LocalAppColors.current
    var selectedDate by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedHour by remember { mutableIntStateOf(initialDateTime.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialDateTime.minute) }

    val scope = rememberCoroutineScope()

    val hourListState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedHour
    )
    val minuteListState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedMinute
    )

    LaunchedEffect(hourListState.isScrollInProgress) {
        if (!hourListState.isScrollInProgress) {
            val idx = hourListState.findCenterItemIndex()
            if (idx != null && idx in 0..23 && idx != selectedHour) {
                selectedHour = idx
            }
            hourListState.animateScrollToItem(selectedHour)
        }
    }

    LaunchedEffect(minuteListState.isScrollInProgress) {
        if (!minuteListState.isScrollInProgress) {
            val idx = minuteListState.findCenterItemIndex()
            if (idx != null && idx in 0..59 && idx != selectedMinute) {
                selectedMinute = idx
            }
            minuteListState.animateScrollToItem(selectedMinute)
        }
    }

    AnimatedDialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "选择日期和时间",
                    style = MaterialTheme.typography.titleLarge,
                    color = appColors.text
                )

                Spacer(modifier = Modifier.height(16.dp))

                CalendarMonthPicker(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = appColors.text.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val wheelHeight = (ITEM_HEIGHT_DP * VISIBLE_ITEMS).dp
                val paddingVertical = (ITEM_HEIGHT_DP * ((VISIBLE_ITEMS - 1) / 2)).dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(wheelHeight),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(ITEM_HEIGHT_DP.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(appColors.primary.copy(alpha = 0.1f))
                        )
                        LazyColumn(
                            state = hourListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(wheelHeight)
                                .fadingEdges(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(vertical = paddingVertical)
                        ) {
                            items(24) { hour ->
                                val isSelected = hour == selectedHour
                                Box(
                                    modifier = Modifier
                                        .height(ITEM_HEIGHT_DP.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedHour = hour
                                            scope.launch { hourListState.animateScrollToItem(hour) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%02d", hour),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) appColors.primary else appColors.text.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center,
                                        fontSize = if (isSelected) 24.sp else 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Text(":", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = appColors.text)

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(ITEM_HEIGHT_DP.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(appColors.primary.copy(alpha = 0.1f))
                        )
                        LazyColumn(
                            state = minuteListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(wheelHeight)
                                .fadingEdges(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(vertical = paddingVertical)
                        ) {
                            items(60) { minute ->
                                val isSelected = minute == selectedMinute
                                Box(
                                    modifier = Modifier
                                        .height(ITEM_HEIGHT_DP.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedMinute = minute
                                            scope.launch { minuteListState.animateScrollToItem(minute) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%02d", minute),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) appColors.primary else appColors.text.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center,
                                        fontSize = if (isSelected) 24.sp else 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                                    hourListState.animateScrollToItem(h)
                                    minuteListState.animateScrollToItem(m)
                                }
                            }
                        ) {
                            Text(timeStr, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onConfirm(LocalDateTime.of(selectedDate, LocalTime.of(selectedHour, selectedMinute)))
                    }) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

/**
 * 保留向后兼容的 ScrollTimePickerDialog（仅选时间）
 */
@Composable
fun ScrollTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    ScrollDateTimePickerDialog(
        initialDateTime = LocalDateTime.of(LocalDate.now(), initialTime),
        onDismiss = onDismiss,
        onConfirm = { dateTime -> onConfirm(dateTime.toLocalTime()) }
    )
}

// ==================== 日历月视图 ====================

@Composable
private fun CalendarMonthPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val appColors = LocalAppColors.current
    var displayMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val today = LocalDate.now()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 月份导航
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { displayMonth = displayMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上月", tint = appColors.text)
            }
            Text(
                text = "${displayMonth.year}年${displayMonth.monthValue}月",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = appColors.text
            )
            IconButton(onClick = { displayMonth = displayMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下月", tint = appColors.text)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 星期标题
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.text.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 日期网格
        val firstDay = displayMonth.atDay(1)
        val dayOfWeekOffset = (firstDay.dayOfWeek.value - 1) // 周一=0
        val daysInMonth = displayMonth.lengthOfMonth()
        val totalCells = dayOfWeekOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - dayOfWeekOffset + 1

                    if (dayNum in 1..daysInMonth) {
                        val date = displayMonth.atDay(dayNum)
                        val isSelected = date == selectedDate
                        val isToday = date == today

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(appColors.primary)
                                    } else if (isToday) {
                                        Modifier.background(appColors.primary.copy(alpha = 0.1f))
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    isToday -> appColors.primary
                                    else -> appColors.text
                                }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

// ==================== 工具函数 ====================

private fun LazyListState.findCenterItemIndex(): Int? {
    val layoutInfo = this.layoutInfo
    if (layoutInfo.visibleItemsInfo.isEmpty()) return null
    val viewportCenter = layoutInfo.viewportStartOffset +
            (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
    return layoutInfo.visibleItemsInfo.minByOrNull { item ->
        abs((item.offset + item.size / 2) - viewportCenter)
    }?.index
}

private fun Modifier.fadingEdges(): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadeHeight = size.height * 0.25f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startY = 0f,
                endY = fadeHeight
            ),
            blendMode = BlendMode.DstIn
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startY = size.height - fadeHeight,
                endY = size.height
            ),
            blendMode = BlendMode.DstIn
        )
    }
