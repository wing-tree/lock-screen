package com.flow.android.kotlin.lockscreen.util

import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateString(simpleDateFormat: SimpleDateFormat): String {
    return simpleDateFormat.format(Date(this))
}

fun <T> List<T>.diff(list: List<T>) = Diff(
        added = list.filterNot { this.contains(it) },
        removed = this.filterNot { list.contains(it) }
)

data class Diff<T>(
        val added: List<T>,
        val removed: List<T>
)