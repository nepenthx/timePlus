/**
 * 数据仓库层
 *
 * 本文件定义了应用的数据仓库（Repository）类，是数据层的统一入口。
 * 仓库模式封装了数据访问逻辑，为 ViewModel 提供干净的数据接口。
 *
 * 主要功能：
 * - 整合多个 DAO 的操作
 * - 提供统一的数据访问接口
 * - 处理数据转换和业务逻辑
 *
 * 架构说明：
 * Repository 是 MVVM 架构中的数据层核心，位于 ViewModel 和 DAO 之间。
 * 它将数据源（数据库、网络等）与 UI 层完全解耦。
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 待办事项数据仓库
 *
 * 整合待办事项、打卡记录、标签和子任务的数据访问。
 * 为上层提供统一的数据操作接口，隐藏底层数据源细节。
 *
 * @property todoDao 待办事项 DAO
 * @property checkInDao 打卡记录 DAO
 * @property tagDao 标签 DAO（可选）
 * @property subTaskDao 子任务 DAO（可选）
 */
class TodoRepository(
    private val todoDao: TodoDao,
    private val checkInDao: CheckInDao,
    private val tagDao: TagDao? = null,
    private val subTaskDao: SubTaskDao? = null
) {
    // ==================== 待办事项相关操作 ====================
    
    /** 获取所有待办事项 */
    fun getAllTodos(): Flow<List<TodoItem>> = todoDao.getAllTodos()

    /** 根据ID获取待办事项 */
    fun getTodoById(id: Long): Flow<TodoItem?> = todoDao.getTodoById(id)

    /**
     * 根据日期获取待办事项
     * @param date 查询日期
     */
    fun getTodosByDate(date: LocalDate): Flow<List<TodoItem>> {
        return todoDao.getTodosByDate(date.toString())
    }

    /**
     * 根据日期范围获取待办事项
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    fun getTodosByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TodoItem>> {
        return todoDao.getTodosByDateRange(startDate.toString(), endDate.toString())
    }

    /** 获取所有周期性待办事项 */
    fun getRecurringTodos(): Flow<List<TodoItem>> = todoDao.getRecurringTodos()

    /** 插入待办事项，返回新记录ID */
    suspend fun insertTodo(todo: TodoItem): Long = todoDao.insertTodo(todo)

    /** 更新待办事项 */
    suspend fun updateTodo(todo: TodoItem) = todoDao.updateTodo(todo)

    /** 删除待办事项 */
    suspend fun deleteTodo(todo: TodoItem) = todoDao.deleteTodo(todo)

    /** 根据ID删除待办事项 */
    suspend fun deleteTodoById(id: Long) = todoDao.deleteTodoById(id)

    // ==================== 子任务相关操作 ====================
    
    /**
     * 根据待办ID获取所有子任务
     * 如果 subTaskDao 为空，返回空列表的 Flow
     */
    fun getSubTasksByTodoId(todoId: Long): Flow<List<SubTask>> {
        return subTaskDao?.getSubTasksByTodoId(todoId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    /** 插入子任务，返回新记录ID */
    suspend fun insertSubTask(subTask: SubTask): Long = subTaskDao?.insertSubTask(subTask) ?: 0

    /** 更新子任务 */
    suspend fun updateSubTask(subTask: SubTask) = subTaskDao?.updateSubTask(subTask)

    /** 删除子任务 */
    suspend fun deleteSubTask(subTask: SubTask) = subTaskDao?.deleteSubTask(subTask)

    /** 删除指定待办的所有子任务 */
    suspend fun deleteSubTasksByTodoId(todoId: Long) = subTaskDao?.deleteSubTasksByTodoId(todoId)

    // ==================== 打卡记录相关操作 ====================
    
    /** 根据待办ID获取所有打卡记录 */
    fun getCheckInsByTodoId(todoId: Long): Flow<List<CheckInRecord>> {
        return checkInDao.getCheckInsByTodoId(todoId)
    }

    /** 根据日期获取所有打卡记录 */
    fun getCheckInsByDate(date: LocalDate): Flow<List<CheckInRecord>> {
        return checkInDao.getCheckInsByDate(date.toString())
    }

    /** 根据日期范围获取打卡记录 */
    fun getCheckInsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<CheckInRecord>> {
        return checkInDao.getCheckInsByDateRange(startDate.toString(), endDate.toString())
    }

    /**
     * 根据待办ID和日期获取打卡记录
     * 用于判断某天是否已打卡
     */
    fun getCheckInByTodoIdAndDate(todoId: Long, date: LocalDate): Flow<CheckInRecord?> {
        return checkInDao.getCheckInByTodoIdAndDate(todoId, date.toString())
    }

    /** 获取待办的总打卡次数 */
    fun getCheckInCountByTodoId(todoId: Long): Flow<Int> {
        return checkInDao.getCheckInCountByTodoId(todoId)
    }

    /** 插入打卡记录 */
    suspend fun insertCheckIn(checkIn: CheckInRecord) = checkInDao.insertCheckIn(checkIn)

    /** 删除打卡记录 */
    suspend fun deleteCheckIn(checkIn: CheckInRecord) = checkInDao.deleteCheckIn(checkIn)

    /**
     * 根据待办ID和日期删除打卡记录
     * 用于取消当天的打卡
     */
    suspend fun deleteCheckInByTodoIdAndDate(todoId: Long, date: LocalDate) {
        checkInDao.deleteCheckInByTodoIdAndDate(todoId, date.toString())
    }

    // ==================== 标签相关操作 ====================
    
    /** 获取所有标签 */
    fun getAllTags(): Flow<List<TodoTag>> = tagDao?.getAllTags() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    
    /** 根据ID获取标签 */
    fun getTagById(id: Long): Flow<TodoTag?> = tagDao?.getTagById(id) ?: kotlinx.coroutines.flow.flowOf(null)
    
    /** 插入标签，返回新记录ID */
    suspend fun insertTag(tag: TodoTag): Long = tagDao?.insertTag(tag) ?: 0
    
    /** 更新标签 */
    suspend fun updateTag(tag: TodoTag) = tagDao?.updateTag(tag)
    
    /** 删除标签 */
    suspend fun deleteTag(tag: TodoTag) = tagDao?.deleteTag(tag)
}
