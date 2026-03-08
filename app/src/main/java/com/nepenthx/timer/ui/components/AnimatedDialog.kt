package com.nepenthx.timer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 带缩放+淡入淡出动画的 Dialog 包装器。
 *
 * 弹出时从 0.85 缩放淡入，关闭时缩小淡出，
 * 同时背景有半透明遮罩的淡入淡出。
 */
@Composable
fun AnimatedDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { animateIn = true }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        AnimatedVisibility(
            visible = animateIn,
            enter = scaleIn(
                initialScale = 0.85f,
                animationSpec = tween(250)
            ) + fadeIn(animationSpec = tween(200)),
            exit = scaleOut(
                targetScale = 0.85f,
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(150))
        ) {
            content()
        }
    }
}
