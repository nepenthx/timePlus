package com.nepenthx.timer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nepenthx.timer.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllNotifications(context)
        }
    }
    
    private fun rescheduleAllNotifications(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val todos = database.todoDao().getAllTodos().first()
                
                todos.filter { it.enableNotification && !it.isCompleted }
                    .forEach { todo ->
                        NotificationHelper.scheduleNotification(context, todo)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
