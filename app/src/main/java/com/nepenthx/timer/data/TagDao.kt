/**
 * 标签数据访问对象（DAO）
 *
 * 本文件定义了标签（TodoTag）的数据库访问接口。
 * 提供标签的增删改查操作，用于管理待办事项的分类标签。
 *
 * 主要功能：
 * - 查询所有标签（按排序顺序）
 * - 根据ID查询标签
 * - 插入、更新、删除标签
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 标签 DAO 接口
 *
 * Room 数据访问对象接口，定义了标签表的所有数据库操作。
 */
@Dao
interface TagDao {
    /**
     * 获取所有标签
     *
     * 查询所有标签，按 sortOrder 字段升序排列。
     * 用于在标签选择器和管理界面显示标签列表。
     *
     * @return 标签列表的 Flow
     */
    @Query("SELECT * FROM todo_tags ORDER BY sortOrder ASC")
    fun getAllTags(): Flow<List<TodoTag>>

    /**
     * 根据ID获取标签
     *
     * @param id 标签ID
     * @return 标签的 Flow，如果不存在则返回 null
     */
    @Query("SELECT * FROM todo_tags WHERE id = :id")
    fun getTagById(id: Long): Flow<TodoTag?>

    /**
     * 插入标签
     *
     * 使用 REPLACE 策略处理冲突。
     *
     * @param tag 要插入的标签
     * @return 新插入记录的行ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TodoTag): Long

    /**
     * 更新标签
     *
     * @param tag 要更新的标签
     */
    @Update
    suspend fun updateTag(tag: TodoTag)

    /**
     * 删除标签（通过对象）
     *
     * @param tag 要删除的标签
     */
    @Delete
    suspend fun deleteTag(tag: TodoTag)

    /**
     * 根据ID删除标签
     *
     * @param id 要删除的标签ID
     */
    @Query("DELETE FROM todo_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)
}
