/**
 * 待办事项数据模型
 *
 * 定义了待办事项（Todo）的核心数据实体类，是整个应用的核心数据结构。
 * 使用 Room 数据库进行持久化存储，支持完整的待办事项管理功能。
 *
 * 主要功能：
 * - 基本待办信息：标题、备注、优先级、完成状态
 * - 时间管理：截止日期时间、创建和更新时间
 * - 周期性任务：支持每日、每周、每月和自定义周天的重复任务
 * - 标签分组：支持按标签对待办进行分类
 * - 通知提醒：可设置提前多少分钟进行通知提醒
 * - 子任务支持：可关联多个子任务
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 待办事项实体类
 *
 * Room 数据库表名：todo_items
 * 这是应用的核心数据模型，表示一个完整的待办事项。
 *
 * @property id 主键ID，自动生成
 * @property title 待办标题，必填项
 * @property note 待办备注/描述信息
 * @property priority 优先级枚举（低/中/高/紧急），默认为中等
 * @property isCompleted 是否已完成
 * @property dueDateTime 截止日期时间
 * @property recurringType 周期类型枚举（无/每日/每周/每月/自定义周）
 * @property recurringEndDate 周期任务的结束日期，null表示无限期
 * @property customWeekDays 自定义周天的位掩码，用于CUSTOM_WEEKLY类型
 * @property tagId 关联的标签ID，null表示未分组
 * @property enableNotification 是否启用通知提醒
 * @property notifyMinutesBefore 提前多少分钟通知，默认15分钟
 * @property hasSubTasks 是否包含子任务
 * @property createdAt 创建时间
 * @property updatedAt 最后更新时间
 */
@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val note: String = "",
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val dueDateTime: LocalDateTime,
    
    // 周期性相关
    val recurringType: RecurringType = RecurringType.NONE,
    val recurringEndDate: LocalDateTime? = null,
    val customWeekDays: Int = 0,
    
    // 标签/分组
    val tagId: Long? = null,
    
    // 通知相关
    val enableNotification: Boolean = false,
    val notifyMinutesBefore: Int = 15,
    
    // 子任务相关
    val hasSubTasks: Boolean = false,
    
    // 软删除（垃圾箱）
    val isDeleted: Boolean = false,
    val deletedAt: LocalDateTime? = null,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 检查某个日期是否应该显示这个待办事项
     *
     * 根据待办的周期类型和截止日期，判断在指定日期是否需要显示此待办。
     * 这是实现周期性任务显示逻辑的核心方法。
     *
     * 判断逻辑：
     * 1. 如果目标日期早于待办的截止日期，不显示
     * 2. 如果存在周期结束日期且目标日期晚于结束日期，不显示
     * 3. 根据周期类型判断：
     *    - NONE（非周期）：仅在截止日期当天显示
     *    - DAILY（每日）：每天都显示
     *    - WEEKLY（每周）：在每周同一天显示
     *    - MONTHLY（每月）：在每月同一天显示
     *    - CUSTOM_WEEKLY（自定义周）：根据customWeekDays位掩码判断
     *
     * @param date 要检查的日期
     * @return 是否应该在该日期显示此待办
     */
    fun shouldShowOnDate(date: java.time.LocalDate): Boolean {
        val todoDate = dueDateTime.toLocalDate()
        
        if (date.isBefore(todoDate)) return false
        if (recurringEndDate != null && date.isAfter(recurringEndDate.toLocalDate())) return false
        
        return when (recurringType) {
            RecurringType.NONE -> date == todoDate
            RecurringType.DAILY -> true
            RecurringType.WEEKLY -> date.dayOfWeek == todoDate.dayOfWeek
            RecurringType.MONTHLY -> date.dayOfMonth == todoDate.dayOfMonth
            RecurringType.CUSTOM_WEEKLY -> WeekDays.isDaySelected(customWeekDays, date.dayOfWeek.value)
        }
    }
    
    /**
     * 判断是否为周期性任务
     *
     * 计算属性，当周期类型不为NONE时返回true。
     * 用于快速判断待办是否为周期性重复任务。
     */
    val isRecurring: Boolean
        get() = recurringType != RecurringType.NONE
}
