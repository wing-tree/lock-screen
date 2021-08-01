package com.flow.android.kotlin.lockscreen.persistence.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flow.android.kotlin.lockscreen.util.BLANK
import com.flow.android.kotlin.lockscreen.util.NEWLINE
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "note")
@Parcelize
data class Note (
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0L,
        var alarmTime: Long = 0L,
        var checklist: Array<ChecklistItem> = arrayOf(),
        var color: Int,
        var content: String,
        var isDone: Boolean = false,
        var modifiedTime: Long,
        @ColumnInfo(defaultValue = "0")
        var priority: Long
) : Parcelable {
    fun contentEquals(note: Note): Boolean {
        if (alarmTime != note.alarmTime)
            return false

        if (checklist.contentEquals(note.checklist).not())
            return false

        if (content != note.content)
            return false

        if (color != note.color)
            return false

        if (isDone != note.isDone)
            return false

        if (modifiedTime != note.modifiedTime)
            return false

        if (priority != note.priority)
            return false

        return true
    }

    fun deepCopy() = Note(
            id = id,
            alarmTime = alarmTime,
            checklist = checklist.copyOf(),
            content = content,
            color = color,
            isDone = isDone,
            modifiedTime = modifiedTime,
            priority = priority
    )

    fun checkListToString() = if (checklist.isEmpty())
        BLANK
    else
        checklist.joinToString("$NEWLINE$NEWLINE")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Note

        if (id != other.id) return false
        if (alarmTime != other.alarmTime) return false
        if (color != other.color) return false
        if (!checklist.contentEquals(other.checklist)) return false
        if (content != other.content) return false
        if (isDone != other.isDone) return false
        if (modifiedTime != other.modifiedTime) return false
        if (priority != other.priority) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + alarmTime.hashCode()
        result = 31 * result + color
        result = 31 * result + checklist.contentHashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + isDone.hashCode()
        result = 31 * result + modifiedTime.hashCode()
        result = 31 * result + priority.hashCode()
        return result
    }
}