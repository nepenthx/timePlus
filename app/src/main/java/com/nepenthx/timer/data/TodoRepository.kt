package com.nepenthx.timer.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TodoRepository(
    private val todoDao: TodoDao,
    private val checkInDao: CheckInDao
) {
    // Todo相关
    fun getAllTodos(): Flow<List<TodoItem>> = todoDao.getAllTodos()

    fun getTodoById(id: Long): Flow<TodoItem?> = todoDao.getTodoById(id)

    fun getTodosByDate(date: LocalDate): Flow<List<TodoItem>> {
        return todoDao.getTodosByDate(date.toString())
    }

    fun getTodosByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TodoItem>> {
        return todoDao.getTodosByDateRange(startDate.toString(), endDate.toString())
    }

    fun getRecurringTodos(): Flow<List<TodoItem>> = todoDao.getRecurringTodos()

    suspend fun insertTodo(todo: TodoItem): Long = todoDao.insertTodo(todo)

    suspend fun updateTodo(todo: TodoItem) = todoDao.updateTodo(todo)

    suspend fun deleteTodo(todo: TodoItem) = todoDao.deleteTodo(todo)

    suspend fun deleteTodoById(id: Long) = todoDao.deleteTodoById(id)

    // CheckIn相关
    fun getCheckInsByTodoId(todoId: Long): Flow<List<CheckInRecord>> {
        return checkInDao.getCheckInsByTodoId(todoId)
    }

    fun getCheckInByTodoIdAndDate(todoId: Long, date: LocalDate): Flow<CheckInRecord?> {
        return checkInDao.getCheckInByTodoIdAndDate(todoId, date.toString())
    }

    fun getCheckInCountByTodoId(todoId: Long): Flow<Int> {
        return checkInDao.getCheckInCountByTodoId(todoId)
    }

    suspend fun insertCheckIn(checkIn: CheckInRecord) = checkInDao.insertCheckIn(checkIn)

    suspend fun deleteCheckIn(checkIn: CheckInRecord) = checkInDao.deleteCheckIn(checkIn)

    suspend fun deleteCheckInByTodoIdAndDate(todoId: Long, date: LocalDate) {
        checkInDao.deleteCheckInByTodoIdAndDate(todoId, date.toString())
    }
}
