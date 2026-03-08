/**
 * Room 数据库配置文件
 *
 * 本文件定义了应用的 Room 数据库实例，是数据持久化层的核心配置。
 * Room 是 SQLite 的抽象层，提供了类型安全的数据库访问能力。
 *
 * 主要功能：
 * - 定义数据库版本和包含的实体表
 * - 配置数据库迁移策略
 * - 提供单例模式的数据库访问方法
 *
 * 数据库版本历史：
 * - 版本 1: 基础待办事项和打卡记录
 * - 版本 2: 添加标签表和tagId字段
 * - 版本 3: 添加自定义周天字段
 * - 版本 4: 添加通知相关字段
 * - 版本 5: 添加子任务表和hasSubTasks字段
 *
 * @author nepenthx
 * @since 1.0
 */
package com.nepenthx.timer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 应用数据库抽象类
 *
 * Room 数据库的主类，定义了数据库的配置和 DAO 访问方法。
 * 使用单例模式确保整个应用只有一个数据库实例。
 *
 * @see TodoDao 待办事项数据访问对象
 * @see CheckInDao 打卡记录数据访问对象
 * @see TagDao 标签数据访问对象
 * @see SubTaskDao 子任务数据访问对象
 */
@Database(
    entities = [TodoItem::class, CheckInRecord::class, TodoTag::class, SubTask::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /** 获取待办事项 DAO */
    abstract fun todoDao(): TodoDao
    /** 获取打卡记录 DAO */
    abstract fun checkInDao(): CheckInDao
    /** 获取标签 DAO */
    abstract fun tagDao(): TagDao
    /** 获取子任务 DAO */
    abstract fun subTaskDao(): SubTaskDao

    companion object {
        /** 数据库单例实例，使用 @Volatile 确保多线程可见性 */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 数据库迁移：版本 1 -> 2
         *
         * 添加标签表（todo_tags）和待办事项的 tagId 字段。
         */
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

        /**
         * 数据库迁移：版本 2 -> 3
         *
         * 添加 customWeekDays 字段，支持自定义周天重复功能。
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todo_items ADD COLUMN customWeekDays INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * 数据库迁移：版本 3 -> 4
         *
         * 添加通知相关字段：enableNotification 和 notifyMinutesBefore。
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todo_items ADD COLUMN enableNotification INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE todo_items ADD COLUMN notifyMinutesBefore INTEGER NOT NULL DEFAULT 15")
            }
        }

        /**
         * 数据库迁移：版本 4 -> 5
         *
         * 添加子任务表（subtasks）和待办事项的 hasSubTasks 字段。
         * 子任务表通过外键关联待办事项，支持级联删除。
         */
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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todo_items ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE todo_items ADD COLUMN deletedAt TEXT DEFAULT NULL")
            }
        }

        /**
         * 获取数据库实例
         *
         * 使用双重检查锁定的单例模式创建数据库实例。
         * 数据库名称为 "todo_database"。
         *
         * 配置说明：
         * - 添加所有版本的迁移策略，确保用户升级时数据不丢失
         * - fallbackToDestructiveMigration：如果迁移失败，允许重建数据库（会丢失数据）
         *
         * @param context 应用上下文
         * @return 数据库实例
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
