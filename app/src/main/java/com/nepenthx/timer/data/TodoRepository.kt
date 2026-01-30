package com.nepenthx.timer.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TodoRepository(
    private val todoDao: TodoDao,
    private val checkInDao: CheckInDao,
    private val tagDao: TagDao? = null,
    private val subTaskDao: SubTaskDao? = null
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

    // SubTask相关
    fun getSubTasksByTodoId(todoId: Long): Flow<List<SubTask>> {
        return subTaskDao?.getSubTasksByTodoId(todoId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun insertSubTask(subTask: SubTask): Long = subTaskDao?.insertSubTask(subTask) ?: 0

    suspend fun updateSubTask(subTask: SubTask) = subTaskDao?.updateSubTask(subTask)

    suspend fun deleteSubTask(subTask: SubTask) = subTaskDao?.deleteSubTask(subTask)

    suspend fun deleteSubTasksByTodoId(todoId: Long) = subTaskDao?.deleteSubTasksByTodoId(todoId)

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

    // Tag相关
    fun getAllTags(): Flow<List<TodoTag>> = tagDao?.getAllTags() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    
    fun getTagById(id: Long): Flow<TodoTag?> = tagDao?.getTagById(id) ?: kotlinx.coroutines.flow.flowOf(null)
    
    suspend fun insertTag(tag: TodoTag): Long = tagDao?.insertTag(tag) ?: 0
    
    suspend fun updateTag(tag: TodoTag) = tagDao?.updateTag(tag)
    
    suspend fun deleteTag(tag: TodoTag) = tagDao?.deleteTag(tag)
}
