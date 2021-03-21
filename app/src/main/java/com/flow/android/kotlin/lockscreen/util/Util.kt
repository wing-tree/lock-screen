package com.flow.android.kotlin.lockscreen.util

import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateString(simpleDateFormat: SimpleDateFormat): String {
    return simpleDateFormat.format(Date(this))
}