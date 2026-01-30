package com.nepenthx.timer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM todo_tags ORDER BY sortOrder ASC")
    fun getAllTags(): Flow<List<TodoTag>>

    @Query("SELECT * FROM todo_tags WHERE id = :id")
    fun getTagById(id: Long): Flow<TodoTag?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TodoTag): Long

    @Update
    suspend fun updateTag(tag: TodoTag)

    @Delete
    suspend fun deleteTag(tag: TodoTag)

    @Query("DELETE FROM todo_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)
}
