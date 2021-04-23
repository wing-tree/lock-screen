package com.flow.android.kotlin.lockscreen.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.flow.android.kotlin.lockscreen.memo.entity.Memo

@Dao
interface MemoDao {
    @Delete
    suspend fun delete(memo: Memo)

    @Transaction
    @Query("SELECT * FROM memo ORDER BY modifiedTime DESC")
    fun getAll(): LiveData<List<Memo>>

    @Insert
    suspend fun insert(memo: Memo)

    @Update
    suspend fun update(memo: Memo)
}