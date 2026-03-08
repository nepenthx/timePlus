package com.nepenthx.timer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.theme.LocalAppColors

/**
 * 侧边栏导航目标
 */
sealed class SidebarDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Today : SidebarDestination("today", "今天", Icons.Outlined.Today, Icons.Filled.Today)
    object Upcoming : SidebarDestination("upcoming", "即将到来", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
    object Anytime : SidebarDestination("anytime", "随时", Icons.Outlined.Inbox, Icons.Filled.Inbox)
    object Completed : SidebarDestination("completed", "已完成", Icons.Outlined.CheckCircle, Icons.Filled.CheckCircle)
    data class TagFilter(val tagId: Long, val tagName: String, val tagColor: Long) : SidebarDestination("tag_$tagId", tagName, Icons.Outlined.Label, Icons.Filled.Label)
    object Trash : SidebarDestination("trash", "垃圾箱", Icons.Outlined.Delete, Icons.Filled.Delete)
    object Settings : SidebarDestination("settings", "设置", Icons.Outlined.Settings, Icons.Filled.Settings)
}

/**
 * 侧边栏内容组件
 */
@Composable
fun SidebarContent(
    selectedDestination: SidebarDestination,
    onDestinationSelected: (SidebarDestination) -> Unit,
    tags: List<TodoTag>,
    todayCount: Int = 0,
    upcomingCount: Int = 0,
    anytimeCount: Int = 0,
    completedCount: Int = 0,
    trashCount: Int = 0,
    tagCounts: Map<Long, Int> = emptyMap()
) {
    val appColors = LocalAppColors.current

    ModalDrawerSheet(
        drawerContainerColor = appColors.background,
        drawerContentColor = appColors.text
    ) {
        // 头部
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = "timePlus",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.primary
            )
        }

        // 智能列表区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            SidebarItem(
                destination = SidebarDestination.Today,
                isSelected = selectedDestination is SidebarDestination.Today,
                onClick = { onDestinationSelected(SidebarDestination.Today) },
                count = todayCount,
                appColors = appColors
            )
            SidebarItem(
                destination = SidebarDestination.Upcoming,
                isSelected = selectedDestination is SidebarDestination.Upcoming,
                onClick = { onDestinationSelected(SidebarDestination.Upcoming) },
                count = upcomingCount,
                appColors = appColors
            )
            SidebarItem(
                destination = SidebarDestination.Anytime,
                isSelected = selectedDestination is SidebarDestination.Anytime,
                onClick = { onDestinationSelected(SidebarDestination.Anytime) },
                count = anytimeCount,
                appColors = appColors
            )
            SidebarItem(
                destination = SidebarDestination.Completed,
                isSelected = selectedDestination is SidebarDestination.Completed,
                onClick = { onDestinationSelected(SidebarDestination.Completed) },
                count = completedCount,
                appColors = appColors
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))

        // 标签列表区域
        Text(
            text = "标签",
            style = MaterialTheme.typography.labelLarge,
            color = appColors.text.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            items(tags) { tag ->
                val destination = SidebarDestination.TagFilter(tag.id, tag.name, tag.color)
                val isSelected = selectedDestination is SidebarDestination.TagFilter && selectedDestination.tagId == tag.id
                
                NavigationDrawerItem(
                    label = { Text(tag.name) },
                    selected = isSelected,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(tag.color))
                        )
                    },
                    badge = {
                        val count = tagCounts[tag.id] ?: 0
                        if (count > 0) {
                            Text(count.toString())
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = appColors.primary.copy(alpha = 0.1f),
                        selectedTextColor = appColors.primary,
                        unselectedTextColor = appColors.text,
                        selectedIconColor = appColors.primary,
                        unselectedIconColor = appColors.text.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        // 底部：垃圾箱 + 设置
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            SidebarItem(
                destination = SidebarDestination.Trash,
                isSelected = selectedDestination is SidebarDestination.Trash,
                onClick = { onDestinationSelected(SidebarDestination.Trash) },
                count = trashCount,
                appColors = appColors
            )
            SidebarItem(
                destination = SidebarDestination.Settings,
                isSelected = selectedDestination is SidebarDestination.Settings,
                onClick = { onDestinationSelected(SidebarDestination.Settings) },
                count = 0,
                appColors = appColors
            )
        }
    }
}

@Composable
private fun SidebarItem(
    destination: SidebarDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    count: Int,
    appColors: com.nepenthx.timer.ui.theme.AppColors
) {
    NavigationDrawerItem(
        label = { Text(destination.title) },
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (isSelected) destination.selectedIcon else destination.icon,
                contentDescription = null
            )
        },
        badge = {
            if (count > 0) {
                AnimatedContent(
                    targetState = count,
                    transitionSpec = {
                        // 数字向上翻转效果
                        if (targetState > initialState) {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                                    slideOutVertically { height -> -height } + fadeOut()
                        } else {
                            slideInVertically { height -> -height } + fadeIn() togetherWith
                                    slideOutVertically { height -> height } + fadeOut()
                        }
                    },
                    label = "BadgeAnimation"
                ) { targetCount ->
                    Text(targetCount.toString())
                }
            }
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = appColors.primary.copy(alpha = 0.1f),
            selectedTextColor = appColors.primary,
            unselectedTextColor = appColors.text,
            selectedIconColor = appColors.primary,
            unselectedIconColor = appColors.text.copy(alpha = 0.6f)
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
