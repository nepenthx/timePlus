/**
 * 打卡记录数据访问对象（DAO）
 *
 * 本文件定义了打卡记录（CheckInRecord）的数据库访问接口。
 * 提供打卡记录的增删查操作，用于实现习惯追踪和打卡功能。
 *
 * 主要功能：
 * - 根据待办ID查询打卡记录
 * - 根据待办ID和日期查询特定打卡记录
 * - 统计打卡次数
 * - 添加和删除打卡记录
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 打卡记录 DAO 接口
 *
 * Room 数据访问对象接口，定义了打卡记录表的所有数据库操作。
 */
@Dao
interface CheckInDao {
    /**
     * 根据待办ID获取所有打卡记录
     *
     * 查询指定待办事项的所有打卡记录，按日期降序排列（最新的在前）。
     * 用于显示打卡历史和统计。
     *
     * @param todoId 待办事项ID
     * @return 打卡记录列表的 Flow
     */
    @Query("SELECT * FROM check_in_records WHERE todoId = :todoId ORDER BY checkInDate DESC")
    fun getCheckInsByTodoId(todoId: Long): Flow<List<CheckInRecord>>

    /**
     * 根据日期获取所有打卡记录
     *
     * 查询指定日期的所有打卡记录，用于判断当天周期任务的完成状态。
     *
     * @param date 日期字符串，格式为 "yyyy-MM-dd"
     * @return 打卡记录列表的 Flow
     */
    @Query("SELECT * FROM check_in_records WHERE checkInDate = :date")
    fun getCheckInsByDate(date: String): Flow<List<CheckInRecord>>

    /**
     * 根据日期范围获取打卡记录
     *
     * 查询指定日期范围内的所有打卡记录，用于周/月视图中还原周期任务的每日完成状态。
     *
     * @param startDate 开始日期，格式为 "yyyy-MM-dd"
     * @param endDate 结束日期，格式为 "yyyy-MM-dd"
     * @return 打卡记录列表的 Flow
     */
    @Query("SELECT * FROM check_in_records WHERE checkInDate BETWEEN :startDate AND :endDate")
    fun getCheckInsByDateRange(startDate: String, endDate: String): Flow<List<CheckInRecord>>

    /**
     * 根据待办ID和日期获取打卡记录
     *
     * 查询指定待办在特定日期是否已打卡。
     * 用于判断当天是否可以打卡或取消打卡。
     *
     * @param todoId 待办事项ID
     * @param date 日期字符串，格式为 "yyyy-MM-dd"
     * @return 打卡记录的 Flow，如果不存在则返回 null
     */
    @Query("SELECT * FROM check_in_records WHERE todoId = :todoId AND checkInDate = :date")
    fun getCheckInByTodoIdAndDate(todoId: Long, date: String): Flow<CheckInRecord?>

    /**
     * 获取待办的打卡总次数
     *
     * 统计指定待办事项的总打卡次数，用于显示打卡统计。
     *
     * @param todoId 待办事项ID
     * @return 打卡次数的 Flow
     */
    @Query("SELECT COUNT(*) FROM check_in_records WHERE todoId = :todoId")
    fun getCheckInCountByTodoId(todoId: Long): Flow<Int>

    /**
     * 插入打卡记录
     *
     * 使用 REPLACE 策略处理冲突，同一待办同一天只能有一条记录。
     *
     * @param checkIn 要插入的打卡记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInRecord)

    /**
     * 删除打卡记录（通过对象）
     *
     * @param checkIn 要删除的打卡记录
     */
    @Delete
    suspend fun deleteCheckIn(checkIn: CheckInRecord)

    /**
     * 根据待办ID和日期删除打卡记录
     *
     * 用于取消当天的打卡。
     *
     * @param todoId 待办事项ID
     * @param date 日期字符串，格式为 "yyyy-MM-dd"
     */
    @Query("DELETE FROM check_in_records WHERE todoId = :todoId AND checkInDate = :date")
    suspend fun deleteCheckInByTodoIdAndDate(todoId: Long, date: String)
}
