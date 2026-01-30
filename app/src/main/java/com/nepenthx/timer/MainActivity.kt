package com.nepenthx.timer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nepenthx.timer.notification.NotificationHelper
import com.nepenthx.timer.ui.screens.AppNavigation
import com.nepenthx.timer.ui.theme.TimerTheme
import com.nepenthx.timer.viewmodel.TodoViewModel

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化通知渠道
        NotificationHelper.createNotificationChannel(this)
        
        // 请求通知权限 (Android 13+)
        requestNotificationPermission()
        
        enableEdgeToEdge()
        setContent {
            val viewModel: TodoViewModel = viewModel()
            val themeSettings by viewModel.themeSettings.collectAsState()
            
            TimerTheme(themeSettings = themeSettings) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}