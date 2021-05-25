package com.flow.android.kotlin.lockscreen.persistence.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "shortcut")
@Parcelize
data class Shortcut (
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,
    var priority: Long,
    @ColumnInfo(name = "show_in_notification")
    var showInNotification: Boolean = false
) : Parcelable