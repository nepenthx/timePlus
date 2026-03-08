/**
 * 待办事项数据访问对象（DAO）
 *
 * 本文件定义了待办事项（TodoItem）的数据库访问接口。
 * 使用 Room 持久化库，提供待办事项的增删改查（CRUD）操作。
 *
 * 主要功能：
 * - 查询所有待办事项
 * - 按ID、日期、日期范围查询待办
 * - 查询周期性待办事项
 * - 插入、更新、删除待办事项
 *
 * 技术特点：
 * - 使用 Kotlin Flow 实现响应式数据更新
 * - 使用协程（suspend）进行异步数据库操作
 * - 支持复杂的 SQL 查询
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 待办事项 DAO 接口
 *
 * Room 数据访问对象接口，定义了待办事项表的所有数据库操作。
 * 所有查询方法返回 Flow，实现数据的响应式更新。
 */
@Dao
interface TodoDao {
    /**
     * 获取所有待办事项
     *
     * 按截止时间升序排列返回所有待办事项列表。
     * 返回 Flow，当数据库数据变化时会自动发出更新。
     *
     * @return 待办事项列表的 Flow
     */
    @Query("SELECT * FROM todo_items WHERE isDeleted = 0 ORDER BY dueDateTime ASC")
    fun getAllTodos(): Flow<List<TodoItem>>

    /**
     * 根据ID获取待办事项
     *
     * @param id 待办事项ID
     * @return 待办事项的 Flow，如果不存在则返回 null
     */
    @Query("SELECT * FROM todo_items WHERE id = :id")
    fun getTodoById(id: Long): Flow<TodoItem?>

    /**
     * 根据日期获取待办事项列表
     *
     * 查询指定日期的所有待办事项，按截止时间和优先级排序。
     *
     * @param date 日期字符串，格式为 "yyyy-MM-dd"
     * @return 该日期的待办事项列表的 Flow
     */
    @Query("""
        SELECT * FROM todo_items 
        WHERE date(dueDateTime) = :date AND isDeleted = 0
        ORDER BY dueDateTime ASC, priority DESC
    """)
    fun getTodosByDate(date: String): Flow<List<TodoItem>>

    /**
     * 根据日期范围获取待办事项列表
     *
     * 查询指定日期范围内的所有待办事项，用于周视图和月视图。
     *
     * @param startDate 开始日期，格式为 "yyyy-MM-dd"
     * @param endDate 结束日期，格式为 "yyyy-MM-dd"
     * @return 日期范围内的待办事项列表的 Flow
     */
    @Query("""
        SELECT * FROM todo_items 
        WHERE date(dueDateTime) BETWEEN :startDate AND :endDate AND isDeleted = 0
        ORDER BY dueDateTime ASC
    """)
    fun getTodosByDateRange(startDate: String, endDate: String): Flow<List<TodoItem>>

    /**
     * 获取所有周期性待办事项
     *
     * 查询所有设置了重复周期的待办事项（recurringType 不为 NONE）。
     * 用于日历视图中计算周期性待办的显示。
     *
     * @return 周期性待办事项列表的 Flow
     */
    @Query("SELECT * FROM todo_items WHERE recurringType != 'NONE' AND isDeleted = 0")
    fun getRecurringTodos(): Flow<List<TodoItem>>

    /**
     * 插入待办事项
     *
     * 使用 REPLACE 策略处理冲突，如果ID已存在则替换。
     *
     * @param todo 要插入的待办事项
     * @return 新插入记录的行ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoItem): Long

    /**
     * 更新待办事项
     *
     * @param todo 要更新的待办事项
     */
    @Update
    suspend fun updateTodo(todo: TodoItem)

    /**
     * 删除待办事项（通过对象）
     *
     * @param todo 要删除的待办事项
     */
    @Delete
    suspend fun deleteTodo(todo: TodoItem)

    /**
     * 根据ID删除待办事项
     *
     * @param id 要删除的待办事项ID
     */
    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteTodoById(id: Long)

    @Query("SELECT * FROM todo_items WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedTodos(): Flow<List<TodoItem>>

    @Query("UPDATE todo_items SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteTodo(id: Long, deletedAt: String)

    @Query("UPDATE todo_items SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreTodo(id: Long)

    @Query("DELETE FROM todo_items WHERE isDeleted = 1 AND deletedAt < :cutoffDate")
    suspend fun purgeOldDeletedTodos(cutoffDate: String)
}
