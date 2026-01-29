package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,              // 待办标题
    val note: String = "",          // 备注
    val priority: Priority = Priority.MEDIUM,  // 优先级
    val isCompleted: Boolean = false,  // 是否完成
    val dueDateTime: LocalDateTime,    // 截止日期时间
    
    // 周期性相关
    val recurringType: RecurringType = RecurringType.NONE,  // 周期类型
    val recurringEndDate: LocalDateTime? = null,  // 周期结束日期
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
