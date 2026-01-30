package com.nepenthx.timer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.ThemePreset
import com.nepenthx.timer.data.ThemeSettings
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.components.TagManagementDialog
import com.nepenthx.timer.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    themeSettings: ThemeSettings,
    tags: List<TodoTag>,
    onPresetSelected: (ThemePreset) -> Unit,
    onCustomColorChange: (
        primaryColor: Long?,
        secondaryColor: Long?,
        backgroundColor: Long?,
        surfaceColor: Long?,
        cardColor: Long?,
        textColor: Long?,
        calendarColor: Long?,
        dateColor: Long?,
        gradientEnabled: Boolean?,
        gradientStartColor: Long?,
        gradientEndColor: Long?
    ) -> Unit,
    onAddTag: (name: String, color: Long) -> Unit,
    onUpdateTag: (TodoTag) -> Unit,
    onDeleteTag: (TodoTag) -> Unit,
    onExportICal: () -> Unit,
    onExportJson: () -> Unit,
    onImportData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    var showColorPicker by remember { mutableStateOf(false) }
    var editingColorType by remember { mutableStateOf<ColorType?>(null) }
    var showTagManagement by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 头部
        item {
            Text(
                text = "我的",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.text
            )
        }

        // 主题设置卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.card)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = appColors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "主题设置",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "预设主题",
                        style = MaterialTheme.typography.titleSmall,
                        color = appColors.text.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 预设主题列表
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ThemePreset.entries.filter { it != ThemePreset.CUSTOM }) { preset ->
                            ThemePresetItem(
                                preset = preset,
                                isSelected = themeSettings.preset == preset,
                                onClick = { onPresetSelected(preset) }
                            )
                        }
                    }
                }
            }
        }

        // 自定义颜色卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.card)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = appColors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "自定义颜色",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 颜色选项网格
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorOptionItem(
                                label = "主色调",
                                color = Color(themeSettings.primaryColor),
                                onClick = {
                                    editingColorType = ColorType.PRIMARY
                                    showColorPicker = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            ColorOptionItem(
                                label = "日历颜色",
                                color = Color(themeSettings.calendarColor),
                                onClick = {
                                    editingColorType = ColorType.CALENDAR
                                    showColorPicker = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorOptionItem(
                                label = "日期颜色",
                                color = Color(themeSettings.dateColor),
                                onClick = {
                                    editingColorType = ColorType.DATE
                                    showColorPicker = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            ColorOptionItem(
                                label = "卡片颜色",
                                color = Color(themeSettings.cardColor),
                                onClick = {
                                    editingColorType = ColorType.CARD
                                    showColorPicker = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorOptionItem(
                                label = "背景颜色",
                                color = Color(themeSettings.backgroundColor),
                                onClick = {
                                    editingColorType = ColorType.BACKGROUND
                                    showColorPicker = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            ColorOptionItem(
                                label = "字体颜色",
                                color = Color(themeSettings.textColor),
                                onClick = {
                                    editingColorType = ColorType.TEXT
                                    showColorPicker = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 渐变色开关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("启用渐变背景", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = themeSettings.gradientEnabled,
                            onCheckedChange = { enabled ->
                                onCustomColorChange(
                                    null, null, null, null, null, null, null, null,
                                    enabled, null, null
                                )
                            }
                        )
                    }
                    
                    if (themeSettings.gradientEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorOptionItem(
                                label = "渐变起始",
                                color = Color(themeSettings.gradientStartColor),
                                onClick = {
                                    editingColorType = ColorType.GRADIENT_START
                                    showColorPicker = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            ColorOptionItem(
                                label = "渐变结束",
                                color = Color(themeSettings.gradientEndColor),
                                onClick = {
                                    editingColorType = ColorType.GRADIENT_END
                                    showColorPicker = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 渐变预览
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(themeSettings.gradientStartColor),
                                            Color(themeSettings.gradientEndColor)
                                        )
                                    )
                                )
                        ) {
                            Text(
                                text = "渐变预览",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 标签管理
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.card)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Label,
                            contentDescription = null,
                            tint = appColors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "标签管理",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "已创建 ${tags.size} 个自定义标签",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.text.copy(alpha = 0.6f)
                    )
                    
                    // 显示现有标签预览
                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tags.take(5)) { tag ->
                                Surface(
                                    color = Color(tag.color).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(tag.color))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tag.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(tag.color)
                                        )
                                    }
                                }
                            }
                            if (tags.size > 5) {
                                item {
                                    Text(
                                        text = "+${tags.size - 5}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = appColors.text.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { showTagManagement = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("管理标签")
                    }
                }
            }
        }

        // 数据管理
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.card)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null,
                            tint = appColors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "数据管理",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "导入导出您的待办数据，支持iCal日历格式和JSON备份格式",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.text.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 导出按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportICal,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出iCal")
                        }
                        OutlinedButton(
                            onClick = onExportJson,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("备份JSON")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 导入按钮
                    Button(
                        onClick = onImportData,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导入数据")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "💡 iCal格式可与Google日历、Outlook等应用兼容",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.text.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // 关于
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.card)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = appColors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "关于",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "待办日历 v1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.text.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // 颜色选择对话框
    if (showColorPicker && editingColorType != null) {
        ColorPickerDialog(
            currentColor = when (editingColorType) {
                ColorType.PRIMARY -> themeSettings.primaryColor
                ColorType.CALENDAR -> themeSettings.calendarColor
                ColorType.DATE -> themeSettings.dateColor
                ColorType.CARD -> themeSettings.cardColor
                ColorType.BACKGROUND -> themeSettings.backgroundColor
                ColorType.TEXT -> themeSettings.textColor
                ColorType.GRADIENT_START -> themeSettings.gradientStartColor
                ColorType.GRADIENT_END -> themeSettings.gradientEndColor
                null -> themeSettings.primaryColor
            },
            onDismiss = { 
                showColorPicker = false
                editingColorType = null
            },
            onColorSelected = { color ->
                when (editingColorType) {
                    ColorType.PRIMARY -> onCustomColorChange(color, null, null, null, null, null, null, null, null, null, null)
                    ColorType.CALENDAR -> onCustomColorChange(null, null, null, null, null, null, color, null, null, null, null)
                    ColorType.DATE -> onCustomColorChange(null, null, null, null, null, null, null, color, null, null, null)
                    ColorType.CARD -> onCustomColorChange(null, null, null, null, color, null, null, null, null, null, null)
                    ColorType.BACKGROUND -> onCustomColorChange(null, null, color, null, null, null, null, null, null, null, null)
                    ColorType.TEXT -> onCustomColorChange(null, null, null, null, null, color, null, null, null, null, null)
                    ColorType.GRADIENT_START -> onCustomColorChange(null, null, null, null, null, null, null, null, null, color, null)
                    ColorType.GRADIENT_END -> onCustomColorChange(null, null, null, null, null, null, null, null, null, null, color)
                    null -> {}
                }
                showColorPicker = false
                editingColorType = null
            }
        )
    }

    // 标签管理对话框
    if (showTagManagement) {
        TagManagementDialog(
            tags = tags,
            onDismiss = { showTagManagement = false },
            onAddTag = onAddTag,
            onUpdateTag = onUpdateTag,
            onDeleteTag = onDeleteTag
        )
    }
}

enum class ColorType {
    PRIMARY, CALENDAR, DATE, CARD, BACKGROUND, TEXT, GRADIENT_START, GRADIENT_END
}

@Composable
private fun ThemePresetItem(
    preset: ThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hasGradient = preset.gradientStartColor != null
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(
                    if (hasGradient) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(preset.gradientStartColor!!),
                                    Color(preset.gradientEndColor!!)
                                )
                            )
                        )
                    } else {
                        Modifier.background(Color(preset.primaryColor))
                    }
                )
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, Color.White, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = preset.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) LocalAppColors.current.primary 
                    else LocalAppColors.current.text.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ColorOptionItem(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = LocalAppColors.current.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ColorPickerDialog(
    currentColor: Long,
    onDismiss: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    // 预定义颜色
    val presetColors = listOf(
        0xFFEF5350, 0xFFEC407A, 0xFFAB47BC, 0xFF7E57C2,
        0xFF5C6BC0, 0xFF42A5F5, 0xFF29B6F6, 0xFF26C6DA,
        0xFF26A69A, 0xFF66BB6A, 0xFF9CCC65, 0xFFD4E157,
        0xFFFFEE58, 0xFFFFCA28, 0xFFFFA726, 0xFFFF7043,
        0xFF8D6E63, 0xFFBDBDBD, 0xFF78909C, 0xFF455A64,
        // 莫兰迪色系
        0xFFB8A9A9, 0xFFD4C4BC, 0xFF8E9AAF, 0xFFB8C5D6,
        0xFF9CAF9C, 0xFFB8C9B8, 0xFFCDB4DB, 0xFFFFC8DD,
        // 更多颜色
        0xFF212121, 0xFF424242, 0xFF616161, 0xFFFFFFFF
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色") },
        text = {
            Column {
                // 颜色网格
                for (row in presetColors.chunked(8)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { colorLong ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorLong))
                                    .clickable { onColorSelected(colorLong) }
                                    .then(
                                        if (colorLong == currentColor) {
                                            Modifier.border(2.dp, Color.White, CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (colorLong == currentColor) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (colorLong == 0xFFFFFFFF.toLong()) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
