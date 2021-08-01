package com.flow.android.kotlin.lockscreen.persistence.dao

import androidx.room.*
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import io.reactivex.Completable

@Dao
interface NoteDao {
    @Delete
    fun delete(note: Note): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note): Completable

    @Update
    fun update(note: Note): Completable

    @Update
    fun updateAll(list: List<Note>): Completable

    @Transaction
    @Query("SELECT * FROM note ORDER BY priority DESC")
    suspend fun getAll(): List<Note>

    @Transaction
    @Query("SELECT * FROM note WHERE alarmTime BETWEEN :start AND :end")
    fun getAll(start: Long, end: Long): List<Note>
}