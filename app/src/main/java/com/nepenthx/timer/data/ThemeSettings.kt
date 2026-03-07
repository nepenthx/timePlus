/**
 * 主题设置相关定义
 *
 * 本文件定义了应用的主题预设和主题配置数据类。
 * 提供多种预设主题供用户选择，并支持自定义主题颜色。
 *
 * 主要功能：
 * - 定义多种预设主题配色方案
 * - 支持自定义主题颜色
 * - 提供主题配置的数据结构
 *
 * 预设主题包括：
 * - DEFAULT: 默认主题（Material 3 紫色系）
 * - MORANDI系列: 莫兰迪色系（灰粉、灰蓝、灰绿）
 * - CLASSIC_BW: 经典黑白主题
 * - DARK_NIGHT: 深邃夜空深色主题
 * - MINT_FRESH: 清新薄荷主题
 * - WARM_SUNSET: 温暖日落主题
 * - SAKURA: 樱花粉主题
 * - CUSTOM: 自定义主题
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.compose.ui.graphics.Color

/**
 * 主题预设枚举类
 *
 * 定义应用的预设主题配色方案，每个主题包含完整的颜色配置。
 *
 * @property displayName 主题显示名称
 * @property primaryColor 主色调，用于按钮、图标等重点元素
 * @property secondaryColor 次要色调，用于辅助元素
 * @property backgroundColor 背景色，用于页面背景
 * @property surfaceColor 表面色，用于卡片、对话框等表面
 * @property cardColor 卡片色，用于待办卡片背景
 * @property textColor 文字色，用于主要文字
 * @property calendarColor 日历色，用于日历相关元素
 * @property dateColor 日期色，用于日期文字
 * @property gradientStartColor 渐变起始色，可选，用于背景渐变
 * @property gradientEndColor 渐变结束色，可选，用于背景渐变
 */
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
    // ==================== 预设主题定义 ====================
    
    /** 默认主题 - Material 3 标准紫色系 */
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
    
    /** 莫兰迪色系 - 灰粉，柔和温馨的粉色系 */
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
    
    /** 莫兰迪色系 - 灰蓝，沉稳宁静的蓝色系 */
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
    
    /** 莫兰迪色系 - 灰绿，清新自然的绿色系 */
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
    
    /** 经典黑白 - 极简主义风格，纯净黑白 */
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
    
    /** 深邃夜空 - 深色主题，适合夜间使用，带渐变背景 */
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
    
    /** 清新薄荷 - 清爽的薄荷绿色系，给人清新感 */
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
    
    /** 温暖日落 - 温暖的橙色系，带渐变背景，给人温馨感 */
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
    
    /** 樱花粉 - 浪漫的粉色系，适合喜欢粉色的用户 */
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
    
    /** 自定义主题 - 用户可自定义所有颜色 */
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

/**
 * 主题设置数据类
 *
 * 存储当前应用的主题配置，可以是预设主题或自定义主题。
 * 当选择预设主题时，各颜色值继承自预设；选择自定义主题时，可独立设置每个颜色。
 *
 * @property preset 当前使用的主题预设
 * @property primaryColor 主色调
 * @property secondaryColor 次要色调
 * @property backgroundColor 背景色
 * @property surfaceColor 表面色
 * @property cardColor 卡片色
 * @property textColor 文字色
 * @property calendarColor 日历色
 * @property dateColor 日期色
 * @property gradientEnabled 是否启用背景渐变
 * @property gradientStartColor 渐变起始色
 * @property gradientEndColor 渐变结束色
 */
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
    /**
     * 将 Long 类型的颜色值转换为 Compose Color 对象
     *
     * @param colorLong ARGB格式的颜色值
     * @return Compose Color 对象
     */
    fun toColor(colorLong: Long): Color = Color(colorLong)
}
