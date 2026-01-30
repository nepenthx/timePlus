package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_tags")
data class TodoTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF6750A4,  // 标签颜色
    val sortOrder: Int = 0          // 排序顺序
)

// 默认分组
val DEFAULT_TAG = TodoTag(
    id = -1,
    name = "默认",
    color = 0xFF6750A4
)
