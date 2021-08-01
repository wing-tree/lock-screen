package com.flow.android.kotlin.lockscreen.persistence.dao

import androidx.room.*
import com.flow.android.kotlin.lockscreen.persistence.entity.AppShortcut
import io.reactivex.Completable

@Dao
interface AppShortcutDao {
    @Delete
    fun delete(appShortcut: AppShortcut): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appShortcut: AppShortcut): Completable

    @Update
    suspend fun updateAll(list: List<AppShortcut>)

    @Transaction
    @Query("SELECT * FROM app_shortcut ORDER BY priority DESC")
    suspend fun getAll(): List<AppShortcut>
}