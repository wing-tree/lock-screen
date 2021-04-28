package com.flow.android.kotlin.lockscreen.memo.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo")
data class Memo (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    var alarmTime: Long = 0L,
    var content: String,
    var isDone: Boolean = false,
    var modifiedTime: Long
)