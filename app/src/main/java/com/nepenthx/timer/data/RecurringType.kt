/**
 * 周期类型枚举定义及周天工具类
 *
 * 本文件定义了待办事项的周期重复类型，以及用于处理自定义周天重复的工具类。
 * 这是实现周期性任务（习惯养成、定期任务）功能的核心模块。
 *
 * 主要功能：
 * - 定义周期类型枚举：无重复、每日、每周、每月、自定义周
 * - 提供周天位掩码常量和工具方法
 * - 支持灵活的自定义周天重复设置
 *
 * 技术实现：
 * - 使用位掩码（Bitmask）存储自定义周天设置，节省存储空间
 * - 通过位运算快速判断某天是否需要显示待办
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

/**
 * 周期重复类型枚举类
 *
 * 定义待办事项的重复周期类型，支持以下五种模式：
 *
 * - NONE: 不重复，一次性任务
 * - DAILY: 每天重复
 * - WEEKLY: 每周重复（在每周的同一天）
 * - MONTHLY: 每月重复（在每月的同一天）
 * - CUSTOM_WEEKLY: 自定义周天重复（用户选择每周的哪几天）
 */
enum class RecurringType {
    NONE,           // 不重复
    DAILY,          // 每天
    WEEKLY,         // 每周（同一天）
    MONTHLY,        // 每月（同一天）
    CUSTOM_WEEKLY   // 自定义每周几天
}

/**
 * 周天工具对象
 *
 * 提供用于处理自定义周天重复的常量和方法。
 * 使用位掩码技术，每个周天对应一个唯一的二进制位。
 *
 * 位掩码设计：
 * - SUNDAY = 1    (二进制: 0000001)
 * - MONDAY = 2    (二进制: 0000010)
 * - TUESDAY = 4   (二进制: 0000100)
 * - WEDNESDAY = 8 (二进制: 0001000)
 * - THURSDAY = 16 (二进制: 0010000)
 * - FRIDAY = 32   (二进制: 0100000)
 * - SATURDAY = 64 (二进制: 1000000)
 *
 * 通过位或运算（|）可以组合多个周天，通过位与运算（&）可以检查是否包含某天。
 */
object WeekDays {
    const val SUNDAY = 1
    const val MONDAY = 2
    const val TUESDAY = 4
    const val WEDNESDAY = 8
    const val THURSDAY = 16
    const val FRIDAY = 32
    const val SATURDAY = 64
    
    /**
     * 所有周天的列表
     *
     * 包含周天常量到中文名称的映射，用于UI显示和选择器。
     * 格式：Pair(位标志, 中文名称)
     */
    val ALL_DAYS = listOf(
        SUNDAY to "周日",
        MONDAY to "周一",
        TUESDAY to "周二",
        WEDNESDAY to "周三",
        THURSDAY to "周四",
        FRIDAY to "周五",
        SATURDAY to "周六"
    )
    
    /**
     * 将 Java DayOfWeek 转换为位标志
     *
     * Java的DayOfWeek使用ISO标准：1=Monday, 7=Sunday
     * 此方法将其转换为应用内使用的位标志格式。
     *
     * @param dayOfWeek Java DayOfWeek值（1-7）
     * @return 对应的位标志值
     */
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
    
    /**
     * 检查某天是否在选中的重复天数中
     *
     * 通过位与运算判断指定的周天是否包含在位掩码中。
     *
     * @param weekDaysFlag 周天位掩码
     * @param dayOfWeek Java DayOfWeek值（1-7）
     * @return 是否选中该天
     */
    fun isDaySelected(weekDaysFlag: Int, dayOfWeek: Int): Boolean {
        val dayFlag = fromDayOfWeek(dayOfWeek)
        return (weekDaysFlag and dayFlag) != 0
    }
    
    /**
     * 获取选中的周天名称列表
     *
     * 从位掩码中解析出所有选中的周天，返回对应的中文名称列表。
     * 用于UI显示用户选择的重复周天。
     *
     * @param weekDaysFlag 周天位掩码
     * @return 选中的周天中文名称列表
     */
    fun getSelectedDays(weekDaysFlag: Int): List<String> {
        return ALL_DAYS.filter { (flag, _) -> (weekDaysFlag and flag) != 0 }
            .map { (_, name) -> name }
    }
}
