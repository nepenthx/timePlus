package com.nepenthx.timer.data

import androidx.compose.ui.graphics.Color

// 预设主题配色
enum class ThemePreset(
    val displayName: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val cardColor: Long,
    val textColor: Long,
    val calendarColor: Long,
    val dateColor: Long,
    val gradientStartColor: Long? = null,
    val gradientEndColor: Long? = null
) {
    // 默认主题
    DEFAULT(
        displayName = "默认",
        primaryColor = 0xFF6750A4,
        secondaryColor = 0xFF625B71,
        backgroundColor = 0xFFFFFBFE,
        surfaceColor = 0xFFFFFBFE,
        cardColor = 0xFFE8DEF8,
        textColor = 0xFF1C1B1F,
        calendarColor = 0xFF6750A4,
        dateColor = 0xFF1C1B1F
    ),
    
    // 莫兰迪色系 - 灰粉
    MORANDI_PINK(
        displayName = "莫兰迪·灰粉",
        primaryColor = 0xFFB8A9A9,
        secondaryColor = 0xFFD4C4BC,
        backgroundColor = 0xFFF5F0ED,
        surfaceColor = 0xFFFAF7F5,
        cardColor = 0xFFE8E0DB,
        textColor = 0xFF5C5252,
        calendarColor = 0xFFB8A9A9,
        dateColor = 0xFF5C5252
    ),
    
    // 莫兰迪色系 - 灰蓝
    MORANDI_BLUE(
        displayName = "莫兰迪·灰蓝",
        primaryColor = 0xFF8E9AAF,
        secondaryColor = 0xFFB8C5D6,
        backgroundColor = 0xFFF0F3F7,
        surfaceColor = 0xFFF7F9FB,
        cardColor = 0xFFDEE5ED,
        textColor = 0xFF4A5568,
        calendarColor = 0xFF8E9AAF,
        dateColor = 0xFF4A5568
    ),
    
    // 莫兰迪色系 - 灰绿
    MORANDI_GREEN(
        displayName = "莫兰迪·灰绿",
        primaryColor = 0xFF9CAF9C,
        secondaryColor = 0xFFB8C9B8,
        backgroundColor = 0xFFF2F5F2,
        surfaceColor = 0xFFF8FAF8,
        cardColor = 0xFFE0E8E0,
        textColor = 0xFF4A5A4A,
        calendarColor = 0xFF9CAF9C,
        dateColor = 0xFF4A5A4A
    ),
    
    // 经典黑白
    CLASSIC_BW(
        displayName = "经典黑白",
        primaryColor = 0xFF212121,
        secondaryColor = 0xFF757575,
        backgroundColor = 0xFFFFFFFF,
        surfaceColor = 0xFFFAFAFA,
        cardColor = 0xFFF5F5F5,
        textColor = 0xFF212121,
        calendarColor = 0xFF212121,
        dateColor = 0xFF212121
    ),
    
    // 深邃夜空
    DARK_NIGHT(
        displayName = "深邃夜空",
        primaryColor = 0xFF7B68EE,
        secondaryColor = 0xFF9370DB,
        backgroundColor = 0xFF1A1A2E,
        surfaceColor = 0xFF16213E,
        cardColor = 0xFF0F3460,
        textColor = 0xFFE8E8E8,
        calendarColor = 0xFF7B68EE,
        dateColor = 0xFFE8E8E8,
        gradientStartColor = 0xFF1A1A2E,
        gradientEndColor = 0xFF16213E
    ),
    
    // 清新薄荷
    MINT_FRESH(
        displayName = "清新薄荷",
        primaryColor = 0xFF26A69A,
        secondaryColor = 0xFF80CBC4,
        backgroundColor = 0xFFE0F2F1,
        surfaceColor = 0xFFF0FAF9,
        cardColor = 0xFFB2DFDB,
        textColor = 0xFF004D40,
        calendarColor = 0xFF26A69A,
        dateColor = 0xFF004D40
    ),
    
    // 温暖日落
    WARM_SUNSET(
        displayName = "温暖日落",
        primaryColor = 0xFFFF7043,
        secondaryColor = 0xFFFFAB91,
        backgroundColor = 0xFFFFF3E0,
        surfaceColor = 0xFFFFFBF5,
        cardColor = 0xFFFFE0B2,
        textColor = 0xFFBF360C,
        calendarColor = 0xFFFF7043,
        dateColor = 0xFFBF360C,
        gradientStartColor = 0xFFFFE0B2,
        gradientEndColor = 0xFFFFF3E0
    ),
    
    // 樱花粉
    SAKURA(
        displayName = "樱花粉",
        primaryColor = 0xFFE91E63,
        secondaryColor = 0xFFF48FB1,
        backgroundColor = 0xFFFCE4EC,
        surfaceColor = 0xFFFFF0F3,
        cardColor = 0xFFF8BBD9,
        textColor = 0xFF880E4F,
        calendarColor = 0xFFE91E63,
        dateColor = 0xFF880E4F
    ),
    
    // 自定义
    CUSTOM(
        displayName = "自定义",
        primaryColor = 0xFF6750A4,
        secondaryColor = 0xFF625B71,
        backgroundColor = 0xFFFFFBFE,
        surfaceColor = 0xFFFFFBFE,
        cardColor = 0xFFE8DEF8,
        textColor = 0xFF1C1B1F,
        calendarColor = 0xFF6750A4,
        dateColor = 0xFF1C1B1F
    )
}

// 主题设置数据类
data class ThemeSettings(
    val preset: ThemePreset = ThemePreset.DEFAULT,
    val primaryColor: Long = preset.primaryColor,
    val secondaryColor: Long = preset.secondaryColor,
    val backgroundColor: Long = preset.backgroundColor,
    val surfaceColor: Long = preset.surfaceColor,
    val cardColor: Long = preset.cardColor,
    val textColor: Long = preset.textColor,
    val calendarColor: Long = preset.calendarColor,
    val dateColor: Long = preset.dateColor,
    val gradientEnabled: Boolean = preset.gradientStartColor != null,
    val gradientStartColor: Long = preset.gradientStartColor ?: preset.backgroundColor,
    val gradientEndColor: Long = preset.gradientEndColor ?: preset.backgroundColor
) {
    fun toColor(colorLong: Long): Color = Color(colorLong)
}
