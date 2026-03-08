package com.nepenthx.timer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sign

private const val SWIPE_THRESHOLD_DP = 100f
private const val MAX_SWIPE_DP = 140f
private const val DAMPING = 0.5f

@Composable
fun SwipeableTaskRow(
    onSwipeToStart: () -> Unit,
    currentDateTime: LocalDateTime,
    onPostpone: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val appColors = LocalAppColors.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val thresholdPx = with(density) { SWIPE_THRESHOLD_DP.dp.toPx() }
    val maxSwipePx = with(density) { MAX_SWIPE_DP.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    var showDateTimePicker by remember { mutableStateOf(false) }

    val absOffset = offsetX.value.absoluteValue
    val progress = (absOffset / thresholdPx).coerceIn(0f, 1f)
    val triggered = absOffset >= thresholdPx
    val isRight = offsetX.value > 0

    Box(modifier = modifier.fillMaxWidth()) {
        // 指示器层：显示在滑出的空白区域
        if (absOffset > 0f) {
            val indicatorWidth = with(density) { absOffset.toDp() }
            val iconTint = if (triggered) appColors.primary else appColors.text.copy(alpha = 0.4f)
            val labelText = if (isRight) {
                if (triggered) "松开推迟" else "推迟"
            } else {
                if (triggered) "松开删除" else "删除"
            }
            val icon = if (isRight) Icons.Default.Schedule else Icons.Default.Delete

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(indicatorWidth)
                    .align(if (isRight) Alignment.CenterStart else Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size((18 + 6 * progress).dp)
                    )
                    if (progress > 0.3f) {
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall,
                            color = iconTint
                        )
                    }
                }
            }
        }

        // 前景层：任务内容跟随偏移
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val current = offsetX.value
                                if (current.absoluteValue >= thresholdPx) {
                                    if (current > 0) {
                                        showDateTimePicker = true
                                    } else {
                                        onSwipeToStart()
                                    }
                                }
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val raw = offsetX.value + dragAmount
                                val damped = applyDamping(raw, thresholdPx, maxSwipePx)
                                offsetX.snapTo(damped)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }

    if (showDateTimePicker) {
        ScrollDateTimePickerDialog(
            initialDateTime = currentDateTime,
            onDismiss = { showDateTimePicker = false },
            onConfirm = { dateTime ->
                onPostpone(dateTime)
                showDateTimePicker = false
            }
        )
    }
}

private fun applyDamping(offset: Float, threshold: Float, max: Float): Float {
    val abs = offset.absoluteValue
    val s = offset.sign
    return if (abs <= threshold) {
        offset
    } else {
        val excess = (abs - threshold) * DAMPING
        val maxExcess = max - threshold
        s * (threshold + excess.coerceAtMost(maxExcess))
    }
}
