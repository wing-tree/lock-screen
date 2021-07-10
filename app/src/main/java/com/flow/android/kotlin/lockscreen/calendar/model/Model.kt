package com.flow.android.kotlin.lockscreen.calendar.model

import androidx.annotation.ColorInt

sealed class Model {
    data class Calendar(
            val id: Long,
            val name: String
    ) : Model()

    data class CalendarEvent (
            val begin: Long,
            @ColorInt
            val calendarColor: Int,
            val calendarDisplayName: String,
            val calendarId: Long,
            val end: Long,
            val eventId: Long,
            val id: Long,
            val title: String
    ) : Model()
}