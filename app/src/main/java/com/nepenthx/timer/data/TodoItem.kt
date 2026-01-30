package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

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
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // 检查某个日期是否应该显示这个重复待办
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
    
    val isRecurring: Boolean
        get() = recurringType != RecurringType.NONE
}
