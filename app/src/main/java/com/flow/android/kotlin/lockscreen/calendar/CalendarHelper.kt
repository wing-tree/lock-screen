package com.flow.android.kotlin.lockscreen.calendar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.CalendarContract
import androidx.annotation.ColorInt
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.util.BLANK
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
                CalendarContract.Calendars.OWNER_ACCOUNT,
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

    private object Instances {
        val projection: Array<String> = arrayOf(
                CalendarContract.Instances._ID,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.CALENDAR_COLOR,
                CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
                CalendarContract.Instances.CALENDAR_ID,
                CalendarContract.Instances.END,
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.RRULE,
                CalendarContract.Instances.TITLE
        )

        object Index {
            @Suppress("ObjectPropertyName")
            const val _ID = 0
            const val ALL_DAY = 1
            const val BEGIN = 2
            const val CALENDAR_COLOR = 3
            const val CALENDAR_DISPLAY_NAME = 4
            const val CALENDAR_ID = 5
            const val END = 6
            const val EVENT_ID = 7
            @Suppress("SpellCheckingInspection")
            const val RRULE = 8
            const val TITLE = 9
        }
    }

    object RequestCode {
        const val EditEvent = 2056
        const val InsertEvent = 2057
    }

    fun calendarDisplays(contentResolver: ContentResolver): List<CalendarDisplay> {
        val calendarDisplays = mutableListOf<CalendarDisplay>()
        val contentUri = CalendarContract.Calendars.CONTENT_URI
        val cursor = contentResolver.query(
                contentUri,
                Calendars.projection,
                null,
                null,
                null
        )

        cursor ?: return emptyList()

        cursor.moveToFirst()

        while (cursor.moveToNext()) {
            @Suppress("LocalVariableName")
            val _id = cursor.getLong(Calendars.Index._ID)
            val calendarDisplayName = cursor.getString(Calendars.Index.CALENDAR_DISPLAY_NAME)
            // val isPrimary = cursor.getString(Calendars.Index.IS_PRIMARY)

            // if (isPrimary == "1")
            calendarDisplays.add(CalendarDisplay(_id, calendarDisplayName))
        }

        cursor.close()

        return calendarDisplays
    }

    @Suppress("SpellCheckingInspection")
    fun instances(contentResolver: ContentResolver, selection: String, DTSTART: Calendar, DTEND: Calendar): ArrayList<Event>? {
        val events = arrayListOf<Event>()

        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, DTSTART.timeInMillis)
        ContentUris.appendId(builder, DTEND.timeInMillis)

        val cursor = contentResolver.query(
                builder.build(),
                Instances.projection,
                selection,
                null,
                null
        )

        cursor ?: return null

        while (cursor.moveToNext()) {
            @Suppress("LocalVariableName")
            val _id = cursor.getLongOrNull(Instances.Index._ID) ?: 0L
            val allDay = cursor.getIntOrNull(Instances.Index.ALL_DAY) ?: 0
            val begin = cursor.getLongOrNull(Instances.Index.BEGIN) ?: 0L
            val calendarColor = cursor.getIntOrNull(Instances.Index.CALENDAR_COLOR) ?: Color.TRANSPARENT
            val calendarDisplayName = cursor.getStringOrNull(Instances.Index.CALENDAR_DISPLAY_NAME) ?: BLANK
            val calendarId = cursor.getLongOrNull(Instances.Index.CALENDAR_ID) ?: continue
            val end = cursor.getLongOrNull(Instances.Index.END) ?: 0L
            val eventId = cursor.getLongOrNull(Instances.Index.EVENT_ID) ?: continue
            val rrule = cursor.getStringOrNull(Instances.Index.RRULE) ?: BLANK
            val title = cursor.getStringOrNull(Instances.Index.TITLE) ?: BLANK

            if (allDay == 1) {
                val gregorianCalendar = GregorianCalendar.getInstance().apply {
                    timeInMillis = DTSTART.timeInMillis
                }

                val yesterday = gregorianCalendar.apply {
                    add(GregorianCalendar.DATE, -1)
                }.time

                if (yesterday.time <= begin && begin <= DTSTART.timeInMillis) {
                    if (DTSTART.timeInMillis <= end && end <= DTEND.timeInMillis)
                        continue
                }
            }

            events.add(Event(
                    begin = begin,
                    calendarColor = calendarColor,
                    calendarDisplayName = calendarDisplayName,
                    calendarId = calendarId,
                    end = end,
                    eventId = eventId,
                    id = _id,
                    rrule = rrule,
                    title = title
            ))
        }

        cursor.close()

        return events
    }

    @SuppressLint("Recycle")
    fun events(contentResolver: ContentResolver, calendarDisplays: List<CalendarDisplay>, amount: Int): ArrayList<Event> {

        val events = arrayListOf<Event>()

        @Suppress("LocalVariableName", "SpellCheckingInspection")
        val DTSTART = Calendar.getInstance()

        @Suppress("LocalVariableName", "SpellCheckingInspection")
        val DTEND = Calendar.getInstance()

        DTSTART.add(Calendar.DATE, amount)
        DTEND.add(Calendar.DATE, amount)

        DTSTART.set(Calendar.HOUR_OF_DAY, 0)
        DTSTART.set(Calendar.MINUTE, 0)
        DTSTART.set(Calendar.SECOND, 0)

        DTEND.set(Calendar.HOUR_OF_DAY, 0)
        DTEND.set(Calendar.MINUTE, 0)
        DTEND.set(Calendar.SECOND, 0)
        DTEND.add(Calendar.DATE, 1)

        val string = calendarDisplays.map { it.id }.joinToString(separator = ", ") { "\"$it\"" }
        val selection = "(${CalendarContract.Instances.CALENDAR_ID} IN ($string))"

        instances(contentResolver, selection, DTSTART, DTEND)?.let { instances ->
            events.addAll(instances)
        }

        return events
    }

    fun editEvent(activity: Activity, event: Event) {
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
        val intent = Intent(Intent.ACTION_INSERT)
                .setData(uri)

        // todo. consider add some flag.
        activity.startActivityForResult(intent, RequestCode.EditEvent)
    }

    fun insertEvent(activity: Activity) {
        val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
        // todo. set flag action..
        val chooser = Intent.createChooser(intent, BLANK)

        activity.startActivityForResult(chooser, RequestCode.InsertEvent)
    }
}

data class CalendarDisplay(
        val id: Long,
        val name: String
)

data class Event(
        val begin: Long,
        @ColorInt
        val calendarColor: Int,
        val calendarDisplayName: String,
        val calendarId: Long,
        val end: Long,
        val eventId: Long,
        val id: Long,
        @Suppress("SpellCheckingInspection")
        val rrule: String,
        val title: String
)