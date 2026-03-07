/**
 * 子任务数据访问对象（DAO）
 *
 * 本文件定义了子任务（SubTask）的数据库访问接口。
 * 提供子任务的增删改查操作，支持与主待办事项的关联查询。
 *
 * 主要功能：
 * - 根据待办ID查询所有子任务
 * - 插入、更新、删除子任务
 * - 批量删除某个待办的所有子任务
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 子任务 DAO 接口
 *
 * Room 数据访问对象接口，定义了子任务表的所有数据库操作。
 */
@Dao
interface SubTaskDao {
    /**
     * 根据待办ID获取所有子任务
     *
     * 查询指定待办事项的所有子任务，按创建时间升序排列。
     *
     * @param todoId 父待办事项的ID
     * @return 子任务列表的 Flow
     */
    @Query("SELECT * FROM subtasks WHERE todoId = :todoId ORDER BY createdAt ASC")
    fun getSubTasksByTodoId(todoId: Long): Flow<List<SubTask>>

    /**
     * 插入子任务
     *
     * 使用 REPLACE 策略处理冲突。
     *
     * @param subTask 要插入的子任务
     * @return 新插入记录的行ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask): Long

    /**
     * 更新子任务
     *
     * @param subTask 要更新的子任务
     */
    @Update
    suspend fun updateSubTask(subTask: SubTask)

    /**
     * 删除子任务
     *
     * @param subTask 要删除的子任务
     */
    @Delete
    suspend fun deleteSubTask(subTask: SubTask)

    /**
     * 删除指定待办的所有子任务
     *
     * 批量删除操作，用于清空某个待办的所有子任务。
     *
     * @param todoId 父待办事项的ID
     */
    @Query("DELETE FROM subtasks WHERE todoId = :todoId")
    suspend fun deleteSubTasksByTodoId(todoId: Long)
}
