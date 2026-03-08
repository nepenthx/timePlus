package com.nepenthx.timer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.theme.LocalAppColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddPanel(
    initialDate: LocalDate = LocalDate.now(),
    initialTagId: Long? = null,
    tags: List<TodoTag> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        note: String,
        priority: Priority,
        dateTime: LocalDateTime,
        recurringType: RecurringType,
        customWeekDays: Int,
        tagId: Long?,
        enableNotification: Boolean,
        notifyMinutesBefore: Int,
        hasSubTasks: Boolean
    ) -> Unit
) {
    val appColors = LocalAppColors.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 表单状态
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var note by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var recurringType by remember { mutableStateOf(RecurringType.NONE) }
    var customWeekDays by remember { mutableStateOf(0) }
    var selectedTagId by remember { mutableStateOf(initialTagId) }
    var enableNotification by remember { mutableStateOf(false) }
    var notifyMinutesBefore by remember { mutableStateOf(15) }
    var hasSubTasks by remember { mutableStateOf(false) }

    var isExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 使用 sheetState 来控制底部面板的展开/收起
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = appColors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        // 关键：windowInsets 设置为空，让内部自行处理键盘避让
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding() // 整个面板内容跟随键盘上移
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // 标题输入框
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("新任务...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = !isExpanded,
                maxLines = if (isExpanded) 3 else 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // 快捷操作栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "日期",
                            tint = if (selectedDate != LocalDate.now()) appColors.primary else appColors.text.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = {
                        priority = when (priority) {
                            Priority.LOW -> Priority.MEDIUM
                            Priority.MEDIUM -> Priority.HIGH
                            Priority.HIGH -> Priority.LOW
                        }
                    }) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = "优先级",
                            tint = when (priority) {
                                Priority.HIGH -> Color(0xFFEF5350)
                                Priority.MEDIUM -> Color(0xFFFFA726)
                                Priority.LOW -> appColors.text.copy(alpha = 0.6f)
                            }
                        )
                    }

                    IconButton(onClick = {
                        if (isExpanded) {
                            isExpanded = false
                        } else {
                            // 展开时先收起键盘，避免键盘遮挡展开内容
                            keyboardController?.hide()
                            isExpanded = true
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "更多",
                            modifier = Modifier.graphicsLayer(rotationZ = if (isExpanded) 180f else 0f),
                            tint = appColors.text.copy(alpha = 0.6f)
                        )
                    }
                }

                // 发送按钮
                FilledIconButton(
                    onClick = {
                        if (title.text.isNotBlank()) {
                            onConfirm(
                                title.text, note, priority,
                                LocalDateTime.of(selectedDate, selectedTime),
                                recurringType, customWeekDays, selectedTagId,
                                enableNotification, notifyMinutesBefore, hasSubTasks
                            )
                            // 发送后关闭面板
                            onDismiss()
                        }
                    },
                    enabled = title.text.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = appColors.primary
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "添加")
                }
            }

            // 展开区域
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = appColors.text.copy(alpha = 0.1f))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    TagSelector(
                        tags = tags,
                        selectedTagId = selectedTagId,
                        onTagSelected = { selectedTagId = it }
                    )

                    RecurringSelector(
                        recurringType = recurringType,
                        customWeekDays = customWeekDays,
                        selectedDate = selectedDate,
                        onRecurringTypeChange = { recurringType = it },
                        onCustomWeekDaysChange = { customWeekDays = it }
                    )

                    NotificationSelector(
                        enableNotification = enableNotification,
                        notifyMinutesBefore = notifyMinutesBefore,
                        onEnableChange = { enableNotification = it },
                        onMinutesChange = { notifyMinutesBefore = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showTimePicker) {
        ScrollDateTimePickerDialog(
            initialDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.now()),
            onDismiss = { showTimePicker = false },
            onConfirm = { dateTime ->
                selectedDate = dateTime.toLocalDate()
                selectedTime = dateTime.toLocalTime()
                showTimePicker = false
            }
        )
    }
}
