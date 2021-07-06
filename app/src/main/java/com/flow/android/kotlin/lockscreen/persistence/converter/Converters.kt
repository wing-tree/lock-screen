package com.flow.android.kotlin.lockscreen.persistence.converter

import androidx.room.TypeConverter
import com.flow.android.kotlin.lockscreen.persistence.data.entity.ChecklistItem
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun arrayToJson(value: Array<ChecklistItem>): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToArray(value: String): Array<ChecklistItem> = Gson().fromJson(value, Array<ChecklistItem>::class.java)
}