package com.nepenthx.timer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 可滑动的任务行组件
 *
 * 包装 TodoItemRow，添加左右滑动交互：
 * - 右滑 (Start to End): 推迟到明天 (蓝色背景 + 时钟图标)
 * - 左滑 (End to Start): 删除任务 (红色背景 + 删除图标)
 *
 * @param content 内部的任务行内容
 * @param onSwipeToStart 左滑回调 (删除)
 * @param onSwipeToEnd 右滑回调 (推迟)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskRow(
    onSwipeToStart: () -> Unit,
    onSwipeToEnd: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeToEnd()
                    // 返回 false 以防止项立即消失，让调用者处理数据更新
                    // 或者如果调用者处理了数据移除，这里返回 true
                    // 通常为了动画连贯性，我们返回 true 让 SwipeToDismissBox 认为操作已确认
                    // 但实际的数据移除可能会导致重组，所以这里需要小心
                    // 如果是推迟，任务可能还在列表中（只是时间变了），所以可能不需要移除
                    // 如果是删除，任务会消失
                    // 这里为了简单，我们返回 true，让组件执行 dismiss 动画，
                    // 同时回调触发数据变更。如果数据变更导致组件移除，Compose 会处理。
                    // 如果数据变更只是修改属性（如推迟），组件会重组。
                    // 为了更好的体验，推迟操作通常不移除行，只是弹回。
                    // 所以对于 StartToEnd (推迟)，我们返回 false 并消费事件
                    false 
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeToStart()
                    true // 删除操作确认移除
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF42A5F5) // Blue for postpone
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF5350) // Red for delete
                    else -> Color.Transparent
                },
                label = "BackgroundColor"
            )
            
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Schedule
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> Icons.Default.Delete
            }
            
            val scale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                label = "IconScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (direction != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.scale(scale),
                        tint = Color.White
                    )
                }
            }
        },
        modifier = modifier,
        content = { content() }
    )
}
