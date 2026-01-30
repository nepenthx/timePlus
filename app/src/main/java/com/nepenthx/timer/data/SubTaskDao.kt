package com.nepenthx.timer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubTaskDao {
    @Query("SELECT * FROM subtasks WHERE todoId = :todoId ORDER BY createdAt ASC")
    fun getSubTasksByTodoId(todoId: Long): Flow<List<SubTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask): Long

    @Update
    suspend fun updateSubTask(subTask: SubTask)

    @Delete
    suspend fun deleteSubTask(subTask: SubTask)

    @Query("DELETE FROM subtasks WHERE todoId = :todoId")
    suspend fun deleteSubTasksByTodoId(todoId: Long)
}
