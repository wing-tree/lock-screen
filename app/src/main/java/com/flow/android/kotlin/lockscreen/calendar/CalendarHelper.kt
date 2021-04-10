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
import com.flow.android.kotlin.lockscreen.util.BLANK
import timber.log.Timber
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

    private object Events {
        val projection: Array<String> = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE
        )

        object Index {
            @Suppress("ObjectPropertyName")
            const val _ID = 0
            const val TITLE = 1
        }
    }

    private object Instances {
        val projection: Array<String> = arrayOf(
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
            const val BEGIN = 0
            const val CALENDAR_COLOR = 1
            const val CALENDAR_DISPLAY_NAME = 2
            const val CALENDAR_ID = 3
            const val END = 4
            const val EVENT_ID = 5
            @Suppress("SpellCheckingInspection")
            const val RRULE = 6
            const val TITLE = 7
        }
    }

    object RequestCode {
        const val EditEvent = 2056
        const val InsertEvent = 2057
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

        Timber.d("calendarDisplays: $calendarDisplays")

        return calendarDisplays
    }

    @Suppress("SpellCheckingInspection")
    @SuppressLint("Recycle")
    fun instances(contentResolver: ContentResolver, eventId: String, DTSTART: Calendar, DTEND: Calendar): ArrayList<Event>? {

        val events = arrayListOf<Event>()

        val selection = "${CalendarContract.Instances.EVENT_ID} = ?"
        val selectionArgs: Array<String> = arrayOf(eventId)

        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, DTSTART.timeInMillis)
        ContentUris.appendId(builder, DTEND.timeInMillis)

        val cursor = contentResolver.query(
                builder.build(),
                Instances.projection,
                selection,
                selectionArgs,
                null
        ) ?: return null

        while (cursor.moveToNext()) {
            val begin = cursor.getLongOrNull(Instances.Index.BEGIN) ?: 0L
            val calendarColor = cursor.getIntOrNull(Instances.Index.CALENDAR_COLOR) ?: Color.TRANSPARENT
            val calendarDisplayName = cursor.getStringOrNull(Instances.Index.CALENDAR_DISPLAY_NAME) ?: BLANK
            val calendarId = cursor.getLongOrNull(Instances.Index.CALENDAR_ID) ?: continue
            val end = cursor.getLongOrNull(Instances.Index.END) ?: 0L
            val id = cursor.getLongOrNull(Instances.Index.EVENT_ID) ?: continue
            val rrule = cursor.getStringOrNull(Instances.Index.RRULE) ?: BLANK
            val title = cursor.getStringOrNull(Instances.Index.TITLE) ?: BLANK

            events.add(Event(
                    begin = begin,
                    calendarColor = calendarColor,
                    calendarDisplayName = calendarDisplayName,
                    calendarId = calendarId,
                    end = end,
                    id = id,
                    rrule = rrule,
                    title = title
            ))
        }

        return events
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
        val selection = "(${CalendarContract.Events.CALENDAR_ID} IN ($string)) AND " +
                "(${CalendarContract.Events.DELETED} = 0)"

        val cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                Events.projection,
                selection,
                null,
                null
        ) ?: return null

        cursor.moveToFirst()

        @Suppress("SpellCheckingInspection")
        while (cursor.moveToNext()) {
            @Suppress("LocalVariableName")
            val _id = cursor.getLongOrNull(Events.Index._ID) ?: continue
            val title = cursor.getStringOrNull(Events.Index.TITLE) ?: BLANK

            Timber.d("events")
            Timber.d("_id: $_id")
            Timber.d("title: $title")

            instances(contentResolver, _id.toString(), DTSTART, DTEND)?.let { instances ->
                events.addAll(instances)
            }
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
        activity.startActivityForResult(intent, RequestCode.InsertEvent)
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
        val id: Long,
        @Suppress("SpellCheckingInspection")
        val rrule: String,
        val title: String
)