package com.nepenthx.timer.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "theme_settings", Context.MODE_PRIVATE
    )

    private val _themeSettings = MutableStateFlow(loadThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

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

    // 加载排序模式
    fun getSortMode(): SortMode {
        val modeName = prefs.getString("sortMode", SortMode.BY_TIME.name) ?: SortMode.BY_TIME.name
        return try {
            SortMode.valueOf(modeName)
        } catch (e: Exception) {
            SortMode.BY_TIME
        }
    }

    fun saveSortMode(mode: SortMode) {
        prefs.edit().putString("sortMode", mode.name).apply()
    }
}
