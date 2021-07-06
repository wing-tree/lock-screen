package com.flow.android.kotlin.lockscreen.persistence.dao

import androidx.room.*
import com.flow.android.kotlin.lockscreen.persistence.data.entity.Memo
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface MemoDao {
    @Delete
    fun delete(memo: Memo): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(memo: Memo): Completable

    @Update
    fun update(memo: Memo): Completable

    @Update
    suspend fun updateAll(list: List<Memo>)

    @Transaction
    @Query("SELECT * FROM memo ORDER BY priority DESC")
    fun getAll(): Flowable<List<Memo>>

    @Transaction
    @Query("SELECT * FROM memo WHERE alarmTime BETWEEN :start AND :end")
    fun getAll(start: Long, end: Long): List<Memo>
}