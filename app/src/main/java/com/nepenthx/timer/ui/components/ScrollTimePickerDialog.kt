package com.nepenthx.timer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nepenthx.timer.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * 滚动式时间选择对话框
 *
 * 提供滚动式的小时和分钟选择器，支持快捷时间按钮。
 * 采用现代化的滚动列表设计，选中的时间高亮显示。
 *
 * @param initialTime 初始时间
 * @param onDismiss 关闭回调
 * @param onConfirm 确认回调，返回选中的时间
 */
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
