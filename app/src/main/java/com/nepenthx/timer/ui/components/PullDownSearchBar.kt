package com.nepenthx.timer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

private const val PULL_THRESHOLD_DP = 100f
private const val MAX_PULL_DP = 140f

@Composable
fun PullDownSearchLayout(
    onSearchTriggered: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val appColors = LocalAppColors.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val pullOffset = remember { Animatable(0f) }
    val thresholdPx = with(density) { PULL_THRESHOLD_DP.dp.toPx() }
    val maxPullPx = with(density) { MAX_PULL_DP.dp.toPx() }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // 只响应手指直接触摸的下拉，忽略 fling 惯性
            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                // 只处理手指触摸滚动，忽略惯性滚动
                if (source != NestedScrollSource.UserInput) {
                    return androidx.compose.ui.geometry.Offset.Zero
                }
                if (available.y > 0 && consumed.y == 0f) {
                    val newOffset = (pullOffset.value + available.y * 0.5f).coerceAtMost(maxPullPx)
                    scope.launch { pullOffset.snapTo(newOffset) }
                    return androidx.compose.ui.geometry.Offset(0f, available.y)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (available.y < 0 && pullOffset.value > 0f) {
                    val consumed = available.y.coerceAtLeast(-pullOffset.value)
                    scope.launch { pullOffset.snapTo((pullOffset.value + consumed).coerceAtLeast(0f)) }
                    return androidx.compose.ui.geometry.Offset(0f, consumed)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val shouldTrigger = pullOffset.value >= thresholdPx
                // 先启动回弹动画，确保视觉流畅
                if (shouldTrigger) {
                    onSearchTriggered()
                }
                pullOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                // 动画完成后再触发回调，避免阻塞动画

                return Velocity.Zero
            }
        }
    }

    val progress = (pullOffset.value / thresholdPx).coerceIn(0f, 1f)
    val triggered = pullOffset.value >= thresholdPx

    Box(modifier = modifier.nestedScroll(nestedScrollConnection)) {
        if (pullOffset.value > 0f) {
            val arcColor = if (triggered) appColors.primary else appColors.primary.copy(alpha = 0.4f)
            val indicatorHeight = with(density) { pullOffset.value.toDp() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(indicatorHeight),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = with(density) { (pullOffset.value * 0.3f).toDp() })
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = if (triggered) appColors.primary else appColors.text.copy(alpha = 0.4f),
                        modifier = Modifier.size((20 + 8 * progress).dp)
                    )
                    if (progress > 0.3f) {
                        Text(
                            text = if (triggered) "松开搜索" else "继续下拉搜索",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (triggered) appColors.primary else appColors.text.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = with(density) { pullOffset.value.toDp() })
        ) {
            content()
        }
    }
}
