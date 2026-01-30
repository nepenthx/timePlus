package com.nepenthx.timer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.nepenthx.timer.data.ThemeSettings

// 自定义主题颜色本地组合
data class AppColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val card: Color,
    val text: Color,
    val calendar: Color,
    val date: Color,
    val gradientEnabled: Boolean,
    val gradientStart: Color,
    val gradientEnd: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        primary = Color(0xFF6750A4),
        secondary = Color(0xFF625B71),
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
        card = Color(0xFFE8DEF8),
        text = Color(0xFF1C1B1F),
        calendar = Color(0xFF6750A4),
        date = Color(0xFF1C1B1F),
        gradientEnabled = false,
        gradientStart = Color(0xFFFFFBFE),
        gradientEnd = Color(0xFFFFFBFE)
    )
}

@Composable
fun TimerTheme(
    themeSettings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val appColors = AppColors(
        primary = Color(themeSettings.primaryColor),
        secondary = Color(themeSettings.secondaryColor),
        background = Color(themeSettings.backgroundColor),
        surface = Color(themeSettings.surfaceColor),
        card = Color(themeSettings.cardColor),
        text = Color(themeSettings.textColor),
        calendar = Color(themeSettings.calendarColor),
        date = Color(themeSettings.dateColor),
        gradientEnabled = themeSettings.gradientEnabled,
        gradientStart = Color(themeSettings.gradientStartColor),
        gradientEnd = Color(themeSettings.gradientEndColor)
    )

    val colorScheme = lightColorScheme(
        primary = appColors.primary,
        secondary = appColors.secondary,
        background = appColors.background,
        surface = appColors.surface,
        surfaceVariant = appColors.card,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = appColors.text,
        onSurface = appColors.text,
        onSurfaceVariant = appColors.text.copy(alpha = 0.7f)
    )

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}