package com.flow.android.kotlin.lockscreen.persistence.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flow.android.kotlin.lockscreen.persistence.converter.Converters
import com.flow.android.kotlin.lockscreen.persistence.dao.NoteDao
import com.flow.android.kotlin.lockscreen.persistence.dao.AppShortcutDao
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import com.flow.android.kotlin.lockscreen.persistence.entity.AppShortcut

@Database(entities = [Note::class, AppShortcut::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appShortcutDao(): AppShortcutDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val name = "com.flow.android.kotlin.lockscreen.persistence.database" +
                ".AppDatabase.name:1.0.3"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note ADD COLUMN color INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE note ADD COLUMN detail TEXT NOT NULL DEFAULT \"\"")
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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note ADD COLUMN checklist TEXT NOT NULL DEFAULT '[]'")
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
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}