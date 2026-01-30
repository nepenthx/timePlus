package com.nepenthx.timer.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.nepenthx.timer.data.Priority
import com.nepenthx.timer.data.RecurringType
import com.nepenthx.timer.data.TodoItem
import com.nepenthx.timer.data.TodoTag
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DataExportHelper {
    
    private val iCalDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    private val jsonDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    // ==================== iCal 格式导出 ====================
    
    fun exportToICal(todos: List<TodoItem>, tags: List<TodoTag>): String {
        val tagMap = tags.associateBy { it.id }
        
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//Nepenthx Timer//Todo Calendar//CN")
        sb.appendLine("CALSCALE:GREGORIAN")
        sb.appendLine("METHOD:PUBLISH")
        sb.appendLine("X-WR-CALNAME:待办日历")
        
        todos.forEach { todo ->
            sb.appendLine("BEGIN:VTODO")
            sb.appendLine("UID:${todo.id}@nepenthx.timer")
            sb.appendLine("DTSTAMP:${LocalDateTime.now().format(iCalDateFormatter)}")
            sb.appendLine("DTSTART:${todo.dueDateTime.format(iCalDateFormatter)}")
            sb.appendLine("DUE:${todo.dueDateTime.format(iCalDateFormatter)}")
            sb.appendLine("SUMMARY:${escapeICalText(todo.title)}")
            
            if (todo.note.isNotEmpty()) {
                sb.appendLine("DESCRIPTION:${escapeICalText(todo.note)}")
            }
            
            val priority = when (todo.priority) {
                Priority.HIGH -> 1
                Priority.MEDIUM -> 5
                Priority.LOW -> 9
            }
            sb.appendLine("PRIORITY:$priority")
            
            val status = if (todo.isCompleted) "COMPLETED" else "NEEDS-ACTION"
            sb.appendLine("STATUS:$status")
            
            val tag = tagMap[todo.tagId]
            if (tag != null) {
                sb.appendLine("CATEGORIES:${escapeICalText(tag.name)}")
            }
            
            val rrule = getRecurrenceRule(todo)
            if (rrule != null) {
                sb.appendLine("RRULE:$rrule")
            }
            
            sb.appendLine("X-NEPENTHX-TAGID:${todo.tagId ?: ""}")
            sb.appendLine("X-NEPENTHX-NOTIFICATION:${todo.enableNotification}")
            sb.appendLine("X-NEPENTHX-NOTIFY-MINUTES:${todo.notifyMinutesBefore}")
            sb.appendLine("X-NEPENTHX-CUSTOM-WEEKDAYS:${todo.customWeekDays}")
            sb.appendLine("X-NEPENTHX-HAS-SUBTASKS:${todo.hasSubTasks}")
            
            sb.appendLine("END:VTODO")
        }
        
        sb.appendLine("END:VCALENDAR")
        return sb.toString()
    }
    
    private fun getRecurrenceRule(todo: TodoItem): String? {
        return when (todo.recurringType) {
            RecurringType.NONE -> null
            RecurringType.DAILY -> "FREQ=DAILY"
            RecurringType.WEEKLY -> "FREQ=WEEKLY"
            RecurringType.MONTHLY -> "FREQ=MONTHLY"
            RecurringType.CUSTOM_WEEKLY -> {
                val days = mutableListOf<String>()
                if ((todo.customWeekDays and 1) != 0) days.add("SU")
                if ((todo.customWeekDays and 2) != 0) days.add("MO")
                if ((todo.customWeekDays and 4) != 0) days.add("TU")
                if ((todo.customWeekDays and 8) != 0) days.add("WE")
                if ((todo.customWeekDays and 16) != 0) days.add("TH")
                if ((todo.customWeekDays and 32) != 0) days.add("FR")
                if ((todo.customWeekDays and 64) != 0) days.add("SA")
                if (days.isNotEmpty()) {
                    "FREQ=WEEKLY;BYDAY=${days.joinToString(",")}"
                } else null
            }
        }
    }
    
    private fun escapeICalText(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
    }
    
    // ==================== iCal 格式导入 ====================
    
    fun importFromICal(content: String): List<TodoItem> {
        val todos = mutableListOf<TodoItem>()
        val lines = content.lines()
        
        var inTodo = false
        var currentTodo = mutableMapOf<String, String>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine == "BEGIN:VTODO" -> {
                    inTodo = true
                    currentTodo = mutableMapOf()
                }
                trimmedLine == "END:VTODO" -> {
                    inTodo = false
                    val todo = parseTodoFromICalMap(currentTodo)
                    if (todo != null) {
                        todos.add(todo)
                    }
                }
                inTodo && trimmedLine.contains(":") -> {
                    val colonIndex = trimmedLine.indexOf(":")
                    val key = trimmedLine.substring(0, colonIndex)
                    val value = trimmedLine.substring(colonIndex + 1)
                    currentTodo[key] = unescapeICalText(value)
                }
            }
        }
        
        return todos
    }
    
    private fun parseTodoFromICalMap(map: Map<String, String>): TodoItem? {
        val title = map["SUMMARY"] ?: return null
        
        val dueDateTime = try {
            val dtString = map["DUE"] ?: map["DTSTART"] ?: return null
            LocalDateTime.parse(dtString.replace("Z", ""), iCalDateFormatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
        
        val priority = when (map["PRIORITY"]?.toIntOrNull() ?: 5) {
            in 1..4 -> Priority.HIGH
            5 -> Priority.MEDIUM
            else -> Priority.LOW
        }
        
        val isCompleted = map["STATUS"] == "COMPLETED"
        val note = map["DESCRIPTION"] ?: ""
        val (recurringType, customWeekDays) = parseRecurrenceRule(map["RRULE"])
        
        val tagId = map["X-NEPENTHX-TAGID"]?.toLongOrNull()
        val enableNotification = map["X-NEPENTHX-NOTIFICATION"]?.toBoolean() ?: false
        val notifyMinutesBefore = map["X-NEPENTHX-NOTIFY-MINUTES"]?.toIntOrNull() ?: 15
        val customWeekDaysFromExt = map["X-NEPENTHX-CUSTOM-WEEKDAYS"]?.toIntOrNull() ?: customWeekDays
        val hasSubTasks = map["X-NEPENTHX-HAS-SUBTASKS"]?.toBoolean() ?: false
        
        return TodoItem(
            id = 0,
            title = title,
            note = note,
            priority = priority,
            isCompleted = isCompleted,
            dueDateTime = dueDateTime,
            recurringType = recurringType,
            customWeekDays = customWeekDaysFromExt,
            tagId = tagId,
            enableNotification = enableNotification,
            notifyMinutesBefore = notifyMinutesBefore,
            hasSubTasks = hasSubTasks
        )
    }
    
    private fun parseRecurrenceRule(rrule: String?): Pair<RecurringType, Int> {
        if (rrule == null) return Pair(RecurringType.NONE, 0)
        
        return when {
            rrule.startsWith("FREQ=DAILY") -> Pair(RecurringType.DAILY, 0)
            rrule.startsWith("FREQ=MONTHLY") -> Pair(RecurringType.MONTHLY, 0)
            rrule.startsWith("FREQ=WEEKLY") -> {
                if (rrule.contains("BYDAY=")) {
                    val byDayMatch = Regex("BYDAY=([A-Z,]+)").find(rrule)
                    val days = byDayMatch?.groupValues?.get(1)?.split(",") ?: emptyList()
                    var customWeekDays = 0
                    days.forEach { day ->
                        customWeekDays = customWeekDays or when (day) {
                            "SU" -> 1
                            "MO" -> 2
                            "TU" -> 4
                            "WE" -> 8
                            "TH" -> 16
                            "FR" -> 32
                            "SA" -> 64
                            else -> 0
                        }
                    }
                    Pair(RecurringType.CUSTOM_WEEKLY, customWeekDays)
                } else {
                    Pair(RecurringType.WEEKLY, 0)
                }
            }
            else -> Pair(RecurringType.NONE, 0)
        }
    }
    
    private fun unescapeICalText(text: String): String {
        return text
            .replace("\\n", "\n")
            .replace("\\,", ",")
            .replace("\\;", ";")
            .replace("\\\\", "\\")
    }
    
    // ==================== JSON 格式 ====================
    
    fun exportToJson(
        todos: List<TodoItem>,
        tags: List<TodoTag>,
        subTasksMap: Map<Long, List<com.nepenthx.timer.data.SubTask>> = emptyMap()
    ): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportTime", LocalDateTime.now().format(jsonDateFormatter))
        root.put("appId", "com.nepenthx.timer")
        
        val tagsArray = JSONArray()
        tags.forEach { tag ->
            val tagObj = JSONObject()
            tagObj.put("id", tag.id)
            tagObj.put("name", tag.name)
            tagObj.put("color", tag.color)
            tagObj.put("sortOrder", tag.sortOrder)
            tagsArray.put(tagObj)
        }
        root.put("tags", tagsArray)
        
        val todosArray = JSONArray()
        todos.forEach { todo ->
            val todoObj = JSONObject()
            todoObj.put("id", todo.id)
            todoObj.put("title", todo.title)
            todoObj.put("note", todo.note)
            todoObj.put("priority", todo.priority.name)
            todoObj.put("isCompleted", todo.isCompleted)
            todoObj.put("dueDateTime", todo.dueDateTime.format(jsonDateFormatter))
            todoObj.put("recurringType", todo.recurringType.name)
            todoObj.put("recurringEndDate", todo.recurringEndDate?.format(jsonDateFormatter))
            todoObj.put("customWeekDays", todo.customWeekDays)
            todoObj.put("tagId", todo.tagId)
            todoObj.put("enableNotification", todo.enableNotification)
            todoObj.put("notifyMinutesBefore", todo.notifyMinutesBefore)
            todoObj.put("hasSubTasks", todo.hasSubTasks)
            todoObj.put("createdAt", todo.createdAt.format(jsonDateFormatter))
            todoObj.put("updatedAt", todo.updatedAt.format(jsonDateFormatter))
            
            val subTasks = subTasksMap[todo.id]
            if (subTasks != null && subTasks.isNotEmpty()) {
                val subTasksArray = JSONArray()
                subTasks.forEach { subTask ->
                    val subTaskObj = JSONObject()
                    subTaskObj.put("title", subTask.title)
                    subTaskObj.put("isCompleted", subTask.isCompleted)
                    subTasksArray.put(subTaskObj)
                }
                todoObj.put("subTasks", subTasksArray)
            }
            
            todosArray.put(todoObj)
        }
        root.put("todos", todosArray)
        
        return root.toString(2)
    }
    
    fun importFromJson(content: String): Pair<List<Pair<TodoItem, List<com.nepenthx.timer.data.SubTask>>>, List<TodoTag>> {
        val todoPairs = mutableListOf<Pair<TodoItem, List<com.nepenthx.timer.data.SubTask>>>()
        val tags = mutableListOf<TodoTag>()
        
        try {
            val root = JSONObject(content)
            
            val tagsArray = root.optJSONArray("tags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    val tagObj = tagsArray.getJSONObject(i)
                    tags.add(TodoTag(
                        id = 0,
                        name = tagObj.getString("name"),
                        color = tagObj.getLong("color"),
                        sortOrder = tagObj.optInt("sortOrder", 0)
                    ))
                }
            }
            
            val todosArray = root.optJSONArray("todos")
            if (todosArray != null) {
                for (i in 0 until todosArray.length()) {
                    val todoObj = todosArray.getJSONObject(i)
                    val todo = TodoItem(
                        id = 0,
                        title = todoObj.getString("title"),
                        note = todoObj.optString("note", ""),
                        priority = Priority.valueOf(todoObj.optString("priority", "MEDIUM")),
                        isCompleted = todoObj.optBoolean("isCompleted", false),
                        dueDateTime = LocalDateTime.parse(todoObj.getString("dueDateTime"), jsonDateFormatter),
                        recurringType = RecurringType.valueOf(todoObj.optString("recurringType", "NONE")),
                        recurringEndDate = todoObj.optString("recurringEndDate", null)?.let { 
                            if (it.isNotEmpty() && it != "null") LocalDateTime.parse(it, jsonDateFormatter) else null
                        },
                        customWeekDays = todoObj.optInt("customWeekDays", 0),
                        tagId = if (todoObj.has("tagId") && !todoObj.isNull("tagId")) todoObj.getLong("tagId") else null,
                        enableNotification = todoObj.optBoolean("enableNotification", false),
                        notifyMinutesBefore = todoObj.optInt("notifyMinutesBefore", 15),
                        hasSubTasks = todoObj.optBoolean("hasSubTasks", false)
                    )
                    
                    val subTasks = mutableListOf<com.nepenthx.timer.data.SubTask>()
                    val subTasksArray = todoObj.optJSONArray("subTasks")
                    if (subTasksArray != null) {
                        for (j in 0 until subTasksArray.length()) {
                            val subTaskObj = subTasksArray.getJSONObject(j)
                            subTasks.add(com.nepenthx.timer.data.SubTask(
                                id = 0,
                                todoId = 0,
                                title = subTaskObj.getString("title"),
                                isCompleted = subTaskObj.optBoolean("isCompleted", false)
                            ))
                        }
                    }
                    
                    todoPairs.add(Pair(todo, subTasks))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return Pair(todoPairs, tags)
    }
    
    // ==================== 文件操作 ====================
    
    fun saveAndShareFile(
        context: Context,
        content: String,
        fileName: String,
        mimeType: String
    ): Uri? {
        return try {
            val file = File(context.cacheDir, fileName)
            file.writeText(content)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "导出待办数据"))
            
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
