package com.flow.android.kotlin.lockscreen.persistence.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.flow.android.kotlin.lockscreen.persistence.dao.MemoDao
import com.flow.android.kotlin.lockscreen.memo.entity.Memo

@Database(entities = [Memo::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        const val name = "com.flow.android.kotlin.lockscreen.persistence.database" +
                ".AppDatabase.name:1.0.0"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            name
                    )
                            .fallbackToDestructiveMigration()
                            .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}