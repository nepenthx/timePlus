/**
 * 主题偏好设置管理
 *
 * 本文件定义了主题设置和排序模式的持久化存储类。
 * 使用 SharedPreferences 存储用户的主题偏好，并通过 StateFlow 提供响应式更新。
 *
 * 主要功能：
 * - 保存和加载主题预设选择
 * - 保存和加载自定义主题颜色
 * - 保存和加载排序模式偏好
 * - 通过 StateFlow 实现主题设置的响应式更新
 *
 * 技术说明：
 * - 使用 SharedPreferences 进行轻量级数据持久化
 * - 使用 StateFlow 实现数据的响应式观察
 * - 支持预设主题和自定义主题两种模式
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 主题偏好设置管理类
 *
 * 负责管理应用的主题设置和排序偏好，使用 SharedPreferences 持久化存储。
 * 通过 StateFlow 暴露主题设置，支持响应式 UI 更新。
 *
 * @property context 应用上下文，用于获取 SharedPreferences
 */
class ThemePreferences(context: Context) {
    /** SharedPreferences 实例，存储名称为 "theme_settings" */
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "theme_settings", Context.MODE_PRIVATE
    )

    /** 主题设置的内部可变状态流 */
    private val _themeSettings = MutableStateFlow(loadThemeSettings())
    
    /** 主题设置的公开状态流，供外部观察 */
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    /**
     * 从 SharedPreferences 加载主题设置
     *
     * 如果是自定义主题，会加载所有自定义颜色值；
     * 如果是预设主题，则使用预设的默认颜色值。
     *
     * @return 加载的 ThemeSettings 对象
     */
    private fun loadThemeSettings(): ThemeSettings {
        val presetName = prefs.getString("preset", ThemePreset.DEFAULT.name) ?: ThemePreset.DEFAULT.name
        val preset = try {
            ThemePreset.valueOf(presetName)
        } catch (e: Exception) {
            ThemePreset.DEFAULT
        }

        return if (preset == ThemePreset.CUSTOM) {
            ThemeSettings(
                preset = preset,
                primaryColor = prefs.getLong("primaryColor", preset.primaryColor),
                secondaryColor = prefs.getLong("secondaryColor", preset.secondaryColor),
                backgroundColor = prefs.getLong("backgroundColor", preset.backgroundColor),
                surfaceColor = prefs.getLong("surfaceColor", preset.surfaceColor),
                cardColor = prefs.getLong("cardColor", preset.cardColor),
                textColor = prefs.getLong("textColor", preset.textColor),
                calendarColor = prefs.getLong("calendarColor", preset.calendarColor),
                dateColor = prefs.getLong("dateColor", preset.dateColor),
                gradientEnabled = prefs.getBoolean("gradientEnabled", false),
                gradientStartColor = prefs.getLong("gradientStartColor", preset.backgroundColor),
                gradientEndColor = prefs.getLong("gradientEndColor", preset.backgroundColor)
            )
        } else {
            ThemeSettings(preset = preset)
        }
    }

    /**
     * 保存主题预设选择
     *
     * 当选择预设主题时，使用预设的默认颜色配置；
     * 当选择自定义主题时，会重新加载已保存的自定义颜色。
     *
     * @param preset 选择的主题预设
     */
    fun savePreset(preset: ThemePreset) {
        prefs.edit().apply {
            putString("preset", preset.name)
            apply()
        }
        _themeSettings.value = if (preset == ThemePreset.CUSTOM) {
            loadThemeSettings()
        } else {
            ThemeSettings(preset = preset)
        }
    }

    /**
     * 保存自定义主题颜色
     *
     * 保存用户自定义的各个颜色值，同时将预设设置为 CUSTOM。
     * 只保存非 null 的颜色值，其他颜色保持不变。
     *
     * @param primaryColor 主色调（可选）
     * @param secondaryColor 次要色调（可选）
     * @param backgroundColor 背景色（可选）
     * @param surfaceColor 表面色（可选）
     * @param cardColor 卡片色（可选）
     * @param textColor 文字色（可选）
     * @param calendarColor 日历色（可选）
     * @param dateColor 日期色（可选）
     * @param gradientEnabled 是否启用渐变（可选）
     * @param gradientStartColor 渐变起始色（可选）
     * @param gradientEndColor 渐变结束色（可选）
     */
    fun saveCustomColor(
        primaryColor: Long? = null,
        secondaryColor: Long? = null,
        backgroundColor: Long? = null,
        surfaceColor: Long? = null,
        cardColor: Long? = null,
        textColor: Long? = null,
        calendarColor: Long? = null,
        dateColor: Long? = null,
        gradientEnabled: Boolean? = null,
        gradientStartColor: Long? = null,
        gradientEndColor: Long? = null
    ) {
        prefs.edit().apply {
            putString("preset", ThemePreset.CUSTOM.name)
            primaryColor?.let { putLong("primaryColor", it) }
            secondaryColor?.let { putLong("secondaryColor", it) }
            backgroundColor?.let { putLong("backgroundColor", it) }
            surfaceColor?.let { putLong("surfaceColor", it) }
            cardColor?.let { putLong("cardColor", it) }
            textColor?.let { putLong("textColor", it) }
            calendarColor?.let { putLong("calendarColor", it) }
            dateColor?.let { putLong("dateColor", it) }
            gradientEnabled?.let { putBoolean("gradientEnabled", it) }
            gradientStartColor?.let { putLong("gradientStartColor", it) }
            gradientEndColor?.let { putLong("gradientEndColor", it) }
            apply()
        }
        _themeSettings.value = loadThemeSettings()
    }

    /**
     * 获取排序模式偏好
     *
     * 从 SharedPreferences 加载用户设置的排序模式。
     * 如果未设置或设置无效，默认返回按时间排序。
     *
     * @return 用户设置的排序模式
     */
    fun getSortMode(): SortMode {
        val modeName = prefs.getString("sortMode", SortMode.BY_TIME.name) ?: SortMode.BY_TIME.name
        return try {
            SortMode.valueOf(modeName)
        } catch (e: Exception) {
            SortMode.BY_TIME
        }
    }

    /**
     * 保存排序模式偏好
     *
     * 将用户选择的排序模式保存到 SharedPreferences。
     *
     * @param mode 要保存的排序模式
     */
    fun saveSortMode(mode: SortMode) {
        prefs.edit().putString("sortMode", mode.name).apply()
    }
}
