/**
 * 打卡记录数据模型
 *
 * 本文件定义了打卡记录（CheckInRecord）的数据实体类。
 * 打卡记录用于追踪周期性待办事项的每日完成情况，是习惯追踪功能的核心数据结构。
 *
 * 主要功能：
 * - 记录待办事项在某一天的打卡状态
 * - 支持为每次打卡添加备注
 * - 用于生成打卡统计和连续打卡天数
 *
 * 使用场景：
 * - 习惯养成追踪（如：每日运动、每日阅读）
 * - 周期性任务的完成记录
 * - 生成打卡日历和统计报表
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 打卡记录实体类
 *
 * Room 数据库表名：check_in_records
 * 记录待办事项在特定日期的打卡信息。
 *
 * @property id 主键ID，自动生成
 * @property todoId 关联的待办事项ID
 * @property checkInDate 打卡日期
 * @property note 打卡备注，可用于记录当天的完成情况或感想
 */
@Entity(tableName = "check_in_records")
data class CheckInRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val todoId: Long,              // 关联的待办ID
    val checkInDate: LocalDate,    // 打卡日期
    val note: String = ""          // 打卡备注
)
