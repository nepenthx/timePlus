package com.nepenthx.timer.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

object DateUtils {
    fun getMonthDays(yearMonth: YearMonth): List<LocalDate?> {
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // 0 = 周日, 1 = 周一, ...
        
        val days = mutableListOf<LocalDate?>()
        
        // 添加前面的空白天
        repeat(firstDayOfWeek) {
            days.add(null)
        }
        
        // 添加本月的天
        var currentDay = firstDay
        while (currentDay <= lastDay) {
            days.add(currentDay)
            currentDay = currentDay.plusDays(1)
        }
        
        return days
    }

    fun getWeekDays(date: LocalDate): List<LocalDate> {
        val sunday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        return (0..6).map { sunday.plusDays(it.toLong()) }
    }

    fun getWeekOfMonth(date: LocalDate): Int {
        val firstDayOfMonth = date.withDayOfMonth(1)
        val daysBetween = ChronoUnit.DAYS.between(firstDayOfMonth, date)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        return ((daysBetween + firstDayOfWeek) / 7).toInt() + 1
    }

    fun formatDate(date: LocalDate, pattern: String = "yyyy年MM月dd日"): String {
        return date.format(DateTimeFormatter.ofPattern(pattern))
    }

    fun formatMonth(yearMonth: YearMonth): String {
        return yearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))
    }
}
