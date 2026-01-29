package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "check_in_records")
data class CheckInRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val todoId: Long,              // 关联的待办ID
    val checkInDate: LocalDate,    // 打卡日期
    val note: String = ""          // 打卡备注
)
