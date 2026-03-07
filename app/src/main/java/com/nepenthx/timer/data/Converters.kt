/**
 * Room 类型转换器
 *
 * 本文件定义了 Room 数据库的类型转换器，用于在 Java/Kotlin 类型
 * 和 SQLite 支持的基本类型之间进行转换。
 *
 * 主要功能：
 * - LocalDateTime 与 String 的相互转换
 * - LocalDate 与 String 的相互转换
 * - Priority 枚举与 String 的相互转换
 * - RecurringType 枚举与 String 的相互转换
 *
 * 使用说明：
 * Room 只支持基本数据类型，对于自定义类型（如 LocalDateTime、枚举等）
 * 需要通过 TypeConverter 进行转换后才能存储到数据库中。
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Room 类型转换器类
 *
 * 提供各种自定义类型与数据库基本类型之间的转换方法。
 * 所有转换方法都使用 @TypeConverter 注解标记。
 */
class Converters {
    /**
     * 将 LocalDateTime 转换为字符串
     *
     * @param value LocalDateTime 值，可为 null
     * @return ISO-8601 格式的日期时间字符串，或 null
     */
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }

    /**
     * 将字符串转换为 LocalDateTime
     *
     * @param value ISO-8601 格式的日期时间字符串，可为 null
     * @return LocalDateTime 对象，或 null
     */
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    /**
     * 将 LocalDate 转换为字符串
     *
     * @param value LocalDate 值，可为 null
     * @return ISO-8601 格式的日期字符串（yyyy-MM-dd），或 null
     */
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    /**
     * 将字符串转换为 LocalDate
     *
     * @param value ISO-8601 格式的日期字符串，可为 null
     * @return LocalDate 对象，或 null
     */
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    /**
     * 将 Priority 枚举转换为字符串
     *
     * 存储枚举的名称（name），如 "LOW"、"MEDIUM"、"HIGH"
     *
     * @param value Priority 枚举值
     * @return 枚举名称字符串
     */
    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }

    /**
     * 将字符串转换为 Priority 枚举
     *
     * @param value 枚举名称字符串
     * @return Priority 枚举值
     */
    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }

    /**
     * 将 RecurringType 枚举转换为字符串
     *
     * 存储枚举的名称（name），如 "NONE"、"DAILY"、"WEEKLY" 等
     *
     * @param value RecurringType 枚举值
     * @return 枚举名称字符串
     */
    @TypeConverter
    fun fromRecurringType(value: RecurringType): String {
        return value.name
    }

    /**
     * 将字符串转换为 RecurringType 枚举
     *
     * @param value 枚举名称字符串
     * @return RecurringType 枚举值
     */
    @TypeConverter
    fun toRecurringType(value: String): RecurringType {
        return RecurringType.valueOf(value)
    }
}
