package com.flow.android.kotlin.lockscreen.persistence.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flow.android.kotlin.lockscreen.util.BLANK
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "memo")
@Parcelize
data class Memo (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    var alarmTime: Long = 0L,
    var color: Int,
    var content: String,
    var detail: String = BLANK,
    var isDone: Boolean = false,
    var modifiedTime: Long,
    @ColumnInfo(defaultValue = "0")
    var priority: Long
) : Parcelable {
    fun contentEquals(memo: Memo): Boolean {
        if (alarmTime != memo.alarmTime)
            return false

        if (content != memo.content)
            return false

        if (color != memo.color)
            return false

        if (isDone != memo.isDone)
            return false

        if (modifiedTime != memo.modifiedTime)
            return false

        if (priority != memo.priority)
            return false

        return true
    }

    fun deepCopy() = Memo(
        id = id,
        alarmTime = alarmTime,
        content = content,
        color = color,
        detail = detail,
        isDone = isDone,
        modifiedTime = modifiedTime,
        priority = priority
    )
}