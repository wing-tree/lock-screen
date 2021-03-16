package com.flow.android.kotlin.lockscreen.calendar

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.*

object CalendarHelper {

    private object Calendars {
        val projection: Array<String> = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.IS_PRIMARY,
                CalendarContract.Calendars.OWNER_ACCOUNT
        )

        object Index {
            @Suppress("ObjectPropertyName")
            const val _ID = 0
            const val ACCOUNT_NAME = 1
            const val ACCOUNT_TYPE = 2
            const val CALENDAR_ACCESS_LEVEL = 3
            const val CALENDAR_DISPLAY_NAME = 4
            const val IS_PRIMARY = 5
            const val OWNER_ACCOUNT = 6
        }
    }

    private object Events {
        val projection: Array<String> = arrayOf(
                CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.RRULE,
                CalendarContract.Events.TITLE
        )

        @Suppress("SpellCheckingInspection")
        object Index {
            const val CALENDAR_DISPLAY_NAME = 0
            const val CALENDAR_ID = 1
            const val DTEND = 2
            const val DTSTART = 3
            const val RRULE = 4
            const val TITLE = 5
        }
    }

    @SuppressLint("Recycle")
    fun calendarDisplays(contentResolver: ContentResolver): List<CalendarDisplay> {
        val contentUri = CalendarContract.Calendars.CONTENT_URI
        val cursor = contentResolver.query(
                contentUri,
                Calendars.projection,
                null,
                null,
                null)

        val calendarDisplays = mutableListOf<CalendarDisplay>()

        cursor?.let {
            cursor.moveToFirst()

            while (cursor.moveToNext()) {
                @Suppress("LocalVariableName")
                val _id = cursor.getLong(Calendars.Index._ID)
                val calendarDisplayName = cursor.getString(Calendars.Index.CALENDAR_DISPLAY_NAME)
                val isPrimary = cursor.getString(Calendars.Index.IS_PRIMARY)

                if (isPrimary == "1")
                    calendarDisplays.add(CalendarDisplay(_id, calendarDisplayName))
            }
        }

        return calendarDisplays
    }

    @SuppressLint("Recycle")
    fun events(contentResolver: ContentResolver, calendarDisplays: List<CalendarDisplay>): ArrayList<Event>? {

        val events = arrayListOf<Event>()

        @Suppress("LocalVariableName", "SpellCheckingInspection")
        val DTSTART = Calendar.getInstance()
        @Suppress("LocalVariableName", "SpellCheckingInspection")
        val DTEND = Calendar.getInstance()

        DTSTART.set(Calendar.HOUR_OF_DAY, 0)
        DTSTART.set(Calendar.MINUTE, 0)
        DTSTART.set(Calendar.SECOND, 0)

        DTEND.set(Calendar.HOUR_OF_DAY, 0)
        DTEND.set(Calendar.MINUTE, 0)
        DTEND.set(Calendar.SECOND, 0)
        DTEND.add(Calendar.DATE, 1)

        val string = calendarDisplays.map { it.id }.joinToString(separator = ", ") { "\"$it\"" }
        val selection = "${CalendarContract.Events.CALENDAR_ID} IN ($string) AND " +
                "(((${CalendarContract.Events.DTSTART} >= ${DTSTART.timeInMillis}) AND " +
                "(${CalendarContract.Events.DTSTART} <=  ${DTEND.timeInMillis})) OR " +
                "((${CalendarContract.Events.DTSTART} <= ${DTSTART.timeInMillis}) AND " +
                "(${CalendarContract.Events.DTEND} >= ${DTSTART.timeInMillis})) OR " +
                "(${CalendarContract.Events.RRULE} != \"\"))"
        val cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                Events.projection,
                selection,
                null,
                null
        ) ?: return null

        cursor.moveToFirst()

        var id = 0L

        @Suppress("SpellCheckingInspection")
        while (cursor.moveToNext()) {
            val calendarDisplayName = cursor.getString(Events.Index.CALENDAR_DISPLAY_NAME)
            val calendarId = cursor.getLong(Events.Index.CALENDAR_ID)
            val dtend = cursor.getLong(Events.Index.DTEND)
            val dtstart = cursor.getLong(Events.Index.DTSTART)
            val rrule = cursor.getString(Events.Index.RRULE)
            val title = cursor.getString(Events.Index.TITLE)

            // todo filter. recurrence ranged calendars.

            if (rrule.isNullOrBlank()) {
                events.add(
                        Event(
                                id = id,
                                calendarDisplayName = calendarDisplayName,
                                calendarId = calendarId,
                                dtend = dtend,
                                dtstart = dtstart,
                                title = title
                        )
                )
            } else {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = dtstart
                }

                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)

                if (DTSTART.get(Calendar.DAY_OF_MONTH) == dayOfMonth && DTSTART.get(Calendar.MONTH) == month) {
                    events.add(
                            Event(
                                    id = id,
                                    calendarDisplayName = calendarDisplayName,
                                    calendarId = calendarId,
                                    dtend = dtend,
                                    dtstart = dtstart,
                                    title = title
                            )
                    )
                }
            }

            ++id
        }

        return events
    }

    fun Long.toDate(): String {
        return SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(Date(this))
    }
}

data class CalendarDisplay(
        val id: Long,
        val name: String
)

@Suppress("SpellCheckingInspection")
data class Event(
        val id: Long,
        val calendarDisplayName: String,
        val calendarId: Long,
        val dtend: Long,
        val dtstart: Long,
        val title: String
)