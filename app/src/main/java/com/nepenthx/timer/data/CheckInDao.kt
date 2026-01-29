package com.nepenthx.timer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_in_records WHERE todoId = :todoId ORDER BY checkInDate DESC")
    fun getCheckInsByTodoId(todoId: Long): Flow<List<CheckInRecord>>

    @Query("SELECT * FROM check_in_records WHERE todoId = :todoId AND checkInDate = :date")
    fun getCheckInByTodoIdAndDate(todoId: Long, date: String): Flow<CheckInRecord?>

    @Query("SELECT COUNT(*) FROM check_in_records WHERE todoId = :todoId")
    fun getCheckInCountByTodoId(todoId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInRecord)

    @Delete
    suspend fun deleteCheckIn(checkIn: CheckInRecord)

    @Query("DELETE FROM check_in_records WHERE todoId = :todoId AND checkInDate = :date")
    suspend fun deleteCheckInByTodoIdAndDate(todoId: Long, date: String)
}
