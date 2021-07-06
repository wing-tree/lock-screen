package com.flow.android.kotlin.lockscreen.persistence.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChecklistItem (
    var id: Long,
    var content: String,
    var done: Boolean
) : Parcelable