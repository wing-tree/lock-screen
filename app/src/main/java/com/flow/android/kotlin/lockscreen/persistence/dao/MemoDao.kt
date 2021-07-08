package com.flow.android.kotlin.lockscreen.persistence.dao

import androidx.room.*
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import io.reactivex.Completable

@Dao
interface MemoDao {
    @Delete
    fun delete(memo: Memo): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(memo: Memo): Completable

    @Update
    fun update(memo: Memo): Completable

    @Update
    fun updateAll(list: List<Memo>): Completable

    @Transaction
    @Query("SELECT * FROM memo ORDER BY priority DESC")
    suspend fun getAll(): List<Memo>

    @Transaction
    @Query("SELECT * FROM memo WHERE alarmTime BETWEEN :start AND :end")
    fun getAll(start: Long, end: Long): List<Memo>
}