package com.flow.android.kotlin.lockscreen.lockscreen.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.calendar.model.Model
import com.flow.android.kotlin.lockscreen.main.view.MainActivity
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.repository.MemoRepository
import com.flow.android.kotlin.lockscreen.util.BLANK
import com.flow.android.kotlin.lockscreen.util.toDateString
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.math.abs

object NotificationBuilder {
    const val ID = 5337800

    private const val PREFIX = "com.flow.android.kotlin.lockscreen.lockscreen.service.NotificationBuilder"
    private const val CHANNEL_ID = "$PREFIX.CHANNEL_ID"

    fun single(context: Context, notificationManager: NotificationManager): Single<NotificationCompat.Builder> {
        return Single.create {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_MIN
                val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)

                notificationChannel.setShowBadge(false)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            GlobalScope.launch(Dispatchers.IO) {
                val calendarEventForNotification = calendarEventForNotification(context)
                val smallIcon = R.drawable.ic_unlock_48px

                val memos = todayMemos(context)
                val memoForNotification = memoForNotification(memos)

                var contentTitle = context.getString(R.string.app_name)
                var contentText = context.getString(R.string.notification_builder_001)
                var subText = BLANK

                val simpleDateFormat = SimpleDateFormat(context.getString(R.string.format_time_002), Locale.getDefault())

                calendarEventForNotification?.let {
                    contentTitle = it.title.take(160)
                    contentText = context.getString(R.string.notification_builder_000)
                    subText = it.begin.toDateString(simpleDateFormat)
                } ?: memoForNotification?.let { memo ->
                    contentTitle = context.getString(R.string.notification_builder_002)
                    contentText = memo.content.take(160)
                    subText = memo.alarmTime.toDateString(simpleDateFormat)
                } ?: run {
                    if (memos.isNotEmpty()) {
                        contentTitle = context.getString(R.string.notification_builder_002)
                        contentText = memos[0].content.take(160)
                    }
                }

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(smallIcon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setShowWhen(false)
                    .setSubText(subText)
                it.onSuccess(builder)
            }
        }
    }

    private fun calendarEventForNotification(context: Context): Model.Event? {
        val contentResolver = context.contentResolver
        var event: Model.Event? = null
        var m = 3.inc()

        val gregorianCalendar = GregorianCalendar().apply {
            timeInMillis = System.currentTimeMillis()
        }

        val currentTimeHourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)

        val calendars = CalendarLoader.calendars(contentResolver)
        val uncheckedCalendarIds = Preference.Calendar.getUncheckedCalendarIds(context)

        val calendarEvents = CalendarLoader.events(context.contentResolver, calendars.filter {
            uncheckedCalendarIds.contains(it.id.toString()).not()
        }, 0)

        calendarEvents.forEach {
            val beginHourOfDay = gregorianCalendar.apply { timeInMillis = it.begin }
                    .get(GregorianCalendar.HOUR_OF_DAY)

            if (beginHourOfDay - 3 <= currentTimeHourOfDay && currentTimeHourOfDay <= beginHourOfDay + 1) {
                val n = abs(currentTimeHourOfDay - beginHourOfDay)

                if (m > n) {
                    m = n
                    event = it
                }
            }
        }

        return event
    }

    private fun todayMemos(context: Context): List<Note> {
        return MemoRepository(context).getTodayMemos()
    }

    private fun memoForNotification(notes: List<Note>): Note? {
        val gregorianCalendar = GregorianCalendar().apply {
            timeInMillis = System.currentTimeMillis()
        }

        val currentTimeHourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)

        notes.sortedWith(Comparator { o1, o2 ->
            return@Comparator when {
                o1.alarmTime - o2.alarmTime < 0 -> 1
                else -> -1
            }
        })

        for (memo in notes) {
            gregorianCalendar.timeInMillis = memo.alarmTime

            val alarmTimeHourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)

            if (alarmTimeHourOfDay - 3 <= currentTimeHourOfDay && currentTimeHourOfDay <= alarmTimeHourOfDay + 3)
                return memo
        }

        return null
    }
}