package com.flow.android.kotlin.lockscreen.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.flow.android.kotlin.lockscreen.persistence.data.entity.Shortcut
import io.reactivex.Completable

@Dao
interface ShortcutDao {
    @Delete
    fun delete(shortcut: Shortcut): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(shortcut: Shortcut): Completable

    @Update
    suspend fun updateAll(list: List<Shortcut>)

    @Transaction
    @Query("SELECT * FROM shortcut ORDER BY priority DESC")
    fun getAll(): LiveData<List<Shortcut>>

    @Transaction
    @Query("SELECT * FROM shortcut ORDER BY priority ASC")
    fun getAllValue(): List<Shortcut>
}