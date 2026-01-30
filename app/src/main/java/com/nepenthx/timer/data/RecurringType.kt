package com.nepenthx.timer.data

enum class RecurringType {
    NONE,           // 不重复
    DAILY,          // 每天
    WEEKLY,         // 每周（同一天）
    MONTHLY,        // 每月（同一天）
    CUSTOM_WEEKLY   // 自定义每周几天
}

// 星期常量（用于自定义每周重复）
object WeekDays {
    const val SUNDAY = 1
    const val MONDAY = 2
    const val TUESDAY = 4
    const val WEDNESDAY = 8
    const val THURSDAY = 16
    const val FRIDAY = 32
    const val SATURDAY = 64
    
    val ALL_DAYS = listOf(
        SUNDAY to "周日",
        MONDAY to "周一",
        TUESDAY to "周二",
        WEDNESDAY to "周三",
        THURSDAY to "周四",
        FRIDAY to "周五",
        SATURDAY to "周六"
    )
    
    // 将DayOfWeek转换为位标志
    fun fromDayOfWeek(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            7 -> SUNDAY  // Java的DayOfWeek: 1=Monday, 7=Sunday
            1 -> MONDAY
            2 -> TUESDAY
            3 -> WEDNESDAY
            4 -> THURSDAY
            5 -> FRIDAY
            6 -> SATURDAY
            else -> 0
        }
    }
    
    // 检查某天是否在重复天数中
    fun isDaySelected(weekDaysFlag: Int, dayOfWeek: Int): Boolean {
        val dayFlag = fromDayOfWeek(dayOfWeek)
        return (weekDaysFlag and dayFlag) != 0
    }
    
    // 获取选中的天数列表
    fun getSelectedDays(weekDaysFlag: Int): List<String> {
        return ALL_DAYS.filter { (flag, _) -> (weekDaysFlag and flag) != 0 }
            .map { (_, name) -> name }
    }
}
