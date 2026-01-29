package com.nepenthx.timer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items ORDER BY dueDateTime ASC")
    fun getAllTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE id = :id")
    fun getTodoById(id: Long): Flow<TodoItem?>

    @Query("""
        SELECT * FROM todo_items 
        WHERE date(dueDateTime) = :date 
        ORDER BY dueDateTime ASC, priority DESC
    """)
    fun getTodosByDate(date: String): Flow<List<TodoItem>>

    @Query("""
        SELECT * FROM todo_items 
        WHERE date(dueDateTime) BETWEEN :startDate AND :endDate 
        ORDER BY dueDateTime ASC
    """)
    fun getTodosByDateRange(startDate: String, endDate: String): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE recurringType != 'NONE'")
    fun getRecurringTodos(): Flow<List<TodoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoItem): Long

    @Update
    suspend fun updateTodo(todo: TodoItem)

    @Delete
    suspend fun deleteTodo(todo: TodoItem)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteTodoById(id: Long)
}
