package com.nepenthx.timer.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nepenthx.timer.MainActivity
import com.nepenthx.timer.R
import com.nepenthx.timer.data.TodoItem
import java.time.ZoneId

object NotificationHelper {
    const val CHANNEL_ID = "todo_reminder_channel"
    const val CHANNEL_NAME = "待办提醒"
    const val CHANNEL_DESCRIPTION = "待办事项提醒通知"
    
    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
            enableLights(true)
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    fun scheduleNotification(context: Context, todo: TodoItem) {
        if (!todo.enableNotification) return
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val notifyTime = todo.dueDateTime.minusMinutes(todo.notifyMinutesBefore.toLong())
        val notifyTimeMillis = notifyTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        if (notifyTimeMillis <= System.currentTimeMillis()) return
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("todo_id", todo.id)
            putExtra("todo_title", todo.title)
            putExtra("todo_note", todo.note)
            putExtra("todo_priority", todo.priority.name)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todo.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notifyTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notifyTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notifyTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notifyTimeMillis,
                pendingIntent
            )
        }
    }
    
    fun cancelNotification(context: Context, todoId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String,
        priority: String
    ) {
        if (!hasNotificationPermission(context)) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val priorityColor = when (priority) {
            "HIGH" -> 0xFFE53935.toInt()
            "MEDIUM" -> 0xFFFFA726.toInt()
            else -> 0xFF66BB6A.toInt()
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(priorityColor)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // 没有通知权限
        }
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra("todo_id", 0)
        val title = intent.getStringExtra("todo_title") ?: "待办提醒"
        val note = intent.getStringExtra("todo_note") ?: ""
        val priority = intent.getStringExtra("todo_priority") ?: "MEDIUM"
        
        val content = if (note.isNotEmpty()) {
            note
        } else {
            "待办事项即将到期"
        }
        
        NotificationHelper.showNotification(
            context,
            todoId.toInt(),
            title,
            content,
            priority
        )
    }
}
