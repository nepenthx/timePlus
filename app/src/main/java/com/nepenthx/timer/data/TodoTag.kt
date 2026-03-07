/**
 * 标签/分组数据模型
 *
 * 本文件定义了待办标签（TodoTag）的数据实体类。
 * 标签用于对待办事项进行分类和分组管理，帮助用户更好地组织和筛选待办。
 *
 * 主要功能：
 * - 为待办事项添加分类标签
 * - 支持自定义标签名称和颜色
 * - 支持标签排序功能
 *
 * 使用场景：
 * - 按项目分类待办（如：工作、学习、生活）
 * - 按优先级或类型标记待办
 * - 通过颜色快速识别不同类型的待办
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 标签实体类
 *
 * Room 数据库表名：todo_tags
 * 用于存储用户自定义的待办标签/分组信息。
 *
 * @property id 主键ID，自动生成
 * @property name 标签名称
 * @property color 标签颜色，使用ARGB格式的Long值，默认为主题紫色
 * @property sortOrder 排序顺序，数值越小越靠前
 */
@Entity(tableName = "todo_tags")
data class TodoTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF6750A4,  // 标签颜色
    val sortOrder: Int = 0          // 排序顺序
)

/**
 * 默认标签常量
 *
 * 用于表示未分组的待办事项的默认标签。
 * ID设为-1以区分数据库中的实际标签记录。
 *
 * 当待办事项的tagId为null时，UI层可使用此默认标签进行显示。
 */
val DEFAULT_TAG = TodoTag(
    id = -1,
    name = "默认",
    color = 0xFF6750A4
)
