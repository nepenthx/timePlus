/**
 * 排序模式枚举定义
 *
 * 本文件定义了待办列表的排序模式枚举类型。
 * 排序模式决定了待办事项在列表中的显示顺序和组织方式。
 *
 * 主要功能：
 * - 按时间排序：根据截止日期时间排序
 * - 按优先级分组：将相同优先级的待办分组显示
 * - 按标签分组：将相同标签的待办分组显示
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

/**
 * 排序模式枚举类
 *
 * 定义待办列表的三种排序/分组模式：
 *
 * - BY_TIME: 按时间排序，待办按截止时间先后排列
 * - BY_PRIORITY: 按优先级分组，相同优先级的待办放在一起
 * - BY_TAG: 按标签分组，相同标签的待办放在一起
 *
 * @property displayName 显示名称，用于UI展示
 */
enum class SortMode(val displayName: String) {
    BY_TIME("按时间排序"),
    BY_PRIORITY("按优先级分组"),
    BY_TAG("按标签分组")
}
