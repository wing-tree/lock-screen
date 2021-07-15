package com.flow.android.kotlin.lockscreen.persistence.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChecklistItem (
    var content: String,
    var id: Long,
    var isDone: Boolean
) : Parcelable