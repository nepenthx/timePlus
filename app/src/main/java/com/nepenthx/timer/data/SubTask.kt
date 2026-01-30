package com.nepenthx.timer.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TodoItem::class,
            parentColumns = ["id"],
            childColumns = ["todoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("todoId")]
)
data class SubTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val todoId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
