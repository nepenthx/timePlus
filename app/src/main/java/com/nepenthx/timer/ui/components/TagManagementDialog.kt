package com.nepenthx.timer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.nepenthx.timer.data.TodoTag
import com.nepenthx.timer.ui.theme.LocalAppColors

@Composable
fun TagManagementDialog(
    tags: List<TodoTag>,
    onDismiss: () -> Unit,
    onAddTag: (name: String, color: Long) -> Unit,
    onUpdateTag: (TodoTag) -> Unit,
    onDeleteTag: (TodoTag) -> Unit
) {
    val appColors = LocalAppColors.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<TodoTag?>(null) }

    AnimatedDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "标签管理",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.text
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 添加新标签按钮
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加新标签")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 标签列表
                if (tags.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Label,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = appColors.text.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "暂无自定义标签",
                                color = appColors.text.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tags, key = { it.id }) { tag ->
                            TagListItem(
                                tag = tag,
                                onEdit = { editingTag = tag },
                                onDelete = { onDeleteTag(tag) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("完成")
                }
            }
        }
    }

    // 添加标签对话框
    if (showAddDialog) {
        AddTagDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                onAddTag(name, color)
                showAddDialog = false
            }
        )
    }

    // 编辑标签对话框
    if (editingTag != null) {
        EditTagDialog(
            tag = editingTag!!,
            onDismiss = { editingTag = null },
            onConfirm = { updatedTag ->
                onUpdateTag(updatedTag)
                editingTag = null
            }
        )
    }
}

@Composable
private fun TagListItem(
    tag: TodoTag,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val appColors = LocalAppColors.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appColors.card.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 标签颜色
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(tag.color))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.text
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = appColors.primary
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除标签 \"${tag.name}\" 吗？删除后相关待办将归入默认分组。") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF6750A4L) }

    val presetColors = listOf(
        0xFF6750A4L, 0xFFEF5350L, 0xFFEC407AL, 0xFFAB47BCL,
        0xFF42A5F5L, 0xFF26A69AL, 0xFF66BB6AL, 0xFFFFA726L,
        0xFF8D6E63L, 0xFF78909CL
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加标签") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("标签名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("选择颜色", style = MaterialTheme.typography.labelMedium)
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presetColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (color == selectedColor) {
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EditTagDialog(
    tag: TodoTag,
    onDismiss: () -> Unit,
    onConfirm: (TodoTag) -> Unit
) {
    var name by remember { mutableStateOf(tag.name) }
    var selectedColor by remember { mutableStateOf(tag.color) }

    val presetColors = listOf(
        0xFF6750A4L, 0xFFEF5350L, 0xFFEC407AL, 0xFFAB47BCL,
        0xFF42A5F5L, 0xFF26A69AL, 0xFF66BB6AL, 0xFFFFA726L,
        0xFF8D6E63L, 0xFF78909CL
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑标签") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("标签名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("选择颜色", style = MaterialTheme.typography.labelMedium)
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presetColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (color == selectedColor) {
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(tag.copy(name = name, color = selectedColor)) },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
