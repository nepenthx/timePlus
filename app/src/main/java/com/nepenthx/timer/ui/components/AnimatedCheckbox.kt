package com.nepenthx.timer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.Priority

/**
 * 自定义动画勾选框
 *
 * 实现了 Things 3 风格的勾选动画：
 * 1. 点击时圆圈会有弹性缩放效果
 * 2. 选中时圆圈颜色填充动画 (drawArc 0->360)
 * 3. 勾号从中心弹出的缩放动画
 *
 * @param checked 是否选中
 * @param onCheckedChange 选中状态改变回调
 * @param priority 优先级，决定边框颜色
 * @param modifier 修饰符
 */
@Composable
fun AnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    priority: Priority,
    modifier: Modifier = Modifier
) {
    // 颜色定义
    val priorityColor = when (priority) {
        Priority.HIGH -> Color(0xFFEF5350)   // Red
        Priority.MEDIUM -> Color(0xFFFFA726) // Orange
        Priority.LOW -> Color(0xFF66BB6A)    // Green
    }
    
    // 动画状态
    val transition = updateTransition(checked, label = "CheckboxTransition")
    
    // 1. 圆圈填充进度 (0f -> 1f)
    val progress by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
            } else {
                spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
            }
        },
        label = "FillProgress"
    ) { state ->
        if (state) 1f else 0f
    }
    
    // 2. 勾号缩放 (0f -> 1f)
    val checkScale by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                // 勾号延迟一点出现，且有弹性
                spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.5f)
            } else {
                tween(durationMillis = 100)
            }
        },
        label = "CheckScale"
    ) { state ->
        if (state) 1f else 0f
    }

    // 交互反馈：点击时的轻微缩放
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 禁用默认波纹，使用自定义动画
                onClick = onCheckedChange
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // 绘制底圈 (未选中时显示)
            if (progress < 1f) {
                drawCircle(
                    color = priorityColor.copy(alpha = 0.3f), // 浅色底圈
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )
                
                // 绘制未选中时的边框 (灰色或优先级颜色)
                // 这里使用优先级颜色，但未选中时可以稍微淡一点或者实心
                drawCircle(
                    color = priorityColor,
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )
            }
            
            // 绘制填充圆 (选中过程)
            if (progress > 0f) {
                // 填充背景
                drawCircle(
                    color = priorityColor,
                    radius = radius * progress, // 从中心扩散填充效果，或者使用 drawArc
                )
                
                // 或者使用 Things 3 的圆环闭合效果
                // drawArc(
                //     color = priorityColor,
                //     startAngle = -90f,
                //     sweepAngle = 360f * progress,
                //     useCenter = false,
                //     style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                //     topLeft = Offset(strokeWidth/2, strokeWidth/2),
                //     size = Size(size.width - strokeWidth, size.height - strokeWidth)
                // )
                // 
                // 如果要填充实心圆：
                // drawCircle(color = priorityColor, radius = radius)
            }
        }
        
        // 勾号图标
        if (checkScale > 0f) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .scale(checkScale)
            )
        }
    }
}
