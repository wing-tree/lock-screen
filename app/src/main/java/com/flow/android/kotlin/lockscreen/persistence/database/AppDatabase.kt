package com.flow.android.kotlin.lockscreen.persistence.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flow.android.kotlin.lockscreen.persistence.dao.MemoDao
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import com.flow.android.kotlin.lockscreen.persistence.dao.ShortcutDao
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut

@Database(entities = [Memo::class, Shortcut::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        const val name = "com.flow.android.kotlin.lockscreen.persistence.database" +
                ".AppDatabase.name:1.0.0"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE memo ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE memo ADD COLUMN color INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE memo ADD COLUMN detail TEXT NOT NULL DEFAULT \"\"")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                CREATE TABLE shortcut (
                    package_name TEXT PRIMARY KEY NOT NULL,
                    priority INTEGER NOT NULL DEFAULT 0,
                    show_in_notification INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            name
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}