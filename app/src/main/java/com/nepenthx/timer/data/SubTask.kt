/**
 * 子任务数据模型
 *
 * 本文件定义了子任务（SubTask）的数据实体类。
 * 子任务是对主待办事项的细分，允许用户将大型任务拆分为更小的可执行步骤。
 *
 * 主要功能：
 * - 支持为待办事项添加多个子任务
 * - 每个子任务可独立标记完成状态
 * - 与主待办事项级联删除（当主待办删除时，所有子任务也会被删除）
 *
 * 使用场景：
 * - 复杂任务的步骤分解
 * - 项目任务的里程碑追踪
 * - 清单类待办的逐项完成
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 子任务实体类
 *
 * Room 数据库表名：subtasks
 * 与 TodoItem 存在一对多关系，通过外键关联并支持级联删除。
 *
 * 外键约束：
 * - 关联 TodoItem 表的 id 字段
 * - 当父待办被删除时，自动删除所有关联的子任务（CASCADE）
 *
 * 索引：
 * - 在 todoId 上建立索引，提高按待办ID查询子任务的性能
 *
 * @property id 主键ID，自动生成
 * @property todoId 关联的父待办事项ID
 * @property title 子任务标题
 * @property isCompleted 是否已完成
 * @property createdAt 创建时间
 */
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TodoItem::class,
            parentColumns = ["id"],
            childColumns = ["todoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("todoId")]
)
data class SubTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val todoId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
