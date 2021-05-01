package com.flow.android.kotlin.lockscreen.memo.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "memo")
@Parcelize
data class Memo (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    var alarmTime: Long = 0L,
    var content: String,
    var isDone: Boolean = false,
    var modifiedTime: Long,
    @ColumnInfo(defaultValue = "0")
    var priority: Long
) : Parcelable {
    fun deepCopy() = Memo(
        id = id,
        alarmTime = alarmTime,
        content = content,
        isDone = isDone,
        modifiedTime = modifiedTime,
        priority = priority
    )
}