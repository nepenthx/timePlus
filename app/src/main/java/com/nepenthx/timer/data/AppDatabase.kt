package com.nepenthx.timer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TodoItem::class, CheckInRecord::class, TodoTag::class, SubTask::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun checkInDao(): CheckInDao
    abstract fun tagDao(): TagDao
    abstract fun subTaskDao(): SubTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS todo_tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color INTEGER NOT NULL DEFAULT 4285132580,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )
                """)
                database.execSQL("ALTER TABLE todo_items ADD COLUMN tagId INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todo_items ADD COLUMN customWeekDays INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todo_items ADD COLUMN enableNotification INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE todo_items ADD COLUMN notifyMinutesBefore INTEGER NOT NULL DEFAULT 15")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todo_items ADD COLUMN hasSubTasks INTEGER NOT NULL DEFAULT 0")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS subtasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        todoId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        createdAt TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(todoId) REFERENCES todo_items(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_todoId ON subtasks (todoId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
