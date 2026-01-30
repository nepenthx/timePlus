package com.nepenthx.timer.data

// 排序模式
enum class SortMode(val displayName: String) {
    BY_TIME("按时间排序"),
    BY_PRIORITY("按优先级分组"),
    BY_TAG("按标签分组")
}
