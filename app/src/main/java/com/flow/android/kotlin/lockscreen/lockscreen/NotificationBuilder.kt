package com.flow.android.kotlin.lockscreen.lockscreen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.main.view.MainActivity
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.repository.Repository
import com.flow.android.kotlin.lockscreen.util.BLANK
import com.flow.android.kotlin.lockscreen.util.toDateString
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

object NotificationBuilder {
    fun single(context: Context, notificationManager: NotificationManager): Single<NotificationCompat.Builder> {
        return Single.create {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_MIN
                val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)

                notificationChannel.description = CHANNEL_DESCRIPTION
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
                val eventForNotification = eventForNotification(context)
                val memos = todayMemos(context)
                val memoForNotification = memoForNotification(memos)

                var contentTitle = context.getString(R.string.app_name)
                var contentText = context.getString(R.string.notification_content_text)
                var subText = BLANK

                val simpleDateFormat = SimpleDateFormat(context.getString(R.string.format_time_01), Locale.getDefault())

                eventForNotification?.let { event ->
                    contentTitle = context.getString(R.string.notification_builder_000)
                    contentText = event.title.take(160)
                    subText = event.begin.toDateString(simpleDateFormat)
                } ?: memoForNotification?.let { memo ->
                    contentTitle = context.getString(R.string.notification_builder_001)
                    contentText = memo.content.take(160)
                    subText = memo.alarmTime.toDateString(simpleDateFormat)
                } ?: run {
                    if (memos.isNotEmpty()) {
                        contentTitle = context.getString(R.string.notification_builder_001)
                        contentText = memos[0].content.take(160)
                    }
                }

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_round_lock_open_24)
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

    private fun eventForNotification(context: Context): Event? {
        val contentResolver = context.contentResolver

        val gregorianCalendar = GregorianCalendar().apply {
            timeInMillis = System.currentTimeMillis()
        }

        val currentTimeHourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)
        val currentTimeMinute = gregorianCalendar.get(GregorianCalendar.MINUTE) + currentTimeHourOfDay * 60

        val calendars = CalendarHelper.calendarDisplays(contentResolver)
        val uncheckedCalendarIds = ConfigurationPreferences.getUncheckedCalendarIds(context)

        val events = CalendarHelper.events(context.contentResolver, calendars.filter {
            uncheckedCalendarIds.contains(it.id.toString()).not()
        }, 0)

        for (event in events) {
            gregorianCalendar.timeInMillis = event.begin

            val beginHourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)
            val beginMinute = gregorianCalendar.get(GregorianCalendar.MINUTE) + beginHourOfDay * 60

            gregorianCalendar.timeInMillis = event.end

            val endHourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)
            val endMinute = gregorianCalendar.get(GregorianCalendar.MINUTE) + endHourOfDay * 60

            if (beginMinute - 10 <= currentTimeMinute && currentTimeMinute <= endMinute + 60) {
                return event
            }
        }

        return null
    }

    private fun todayMemos(context: Context): List<Memo> {
        return Repository(context).getTodayMemos()
    }

    private fun memoForNotification(memos: List<Memo>): Memo? {
        val gregorianCalendar = GregorianCalendar().apply {
            timeInMillis = System.currentTimeMillis()
        }

        val currentTimeHourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)
        val currentTimeMinute = gregorianCalendar.get(GregorianCalendar.MINUTE) + currentTimeHourOfDay * 60

        memos.sortedWith(Comparator { o1, o2 ->
            return@Comparator when {
                o1.alarmTime - o2.alarmTime < 0 -> 1
                else -> -1
            }
        })

        for (memo in memos) {
            gregorianCalendar.timeInMillis = memo.alarmTime

            val alarmTimeHourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)
            val alarmTimeMinute = gregorianCalendar.get(GregorianCalendar.MINUTE) + alarmTimeHourOfDay * 60

            if (alarmTimeMinute - 10 <= currentTimeMinute && currentTimeMinute <= alarmTimeMinute + 60)
                return memo
        }

        return null
    }

    private const val CHANNEL_NAME = "com.flow.android.kotlin.lockscreen.lock_screen.channel_name"
    private const val CHANNEL_DESCRIPTION = "com.flow.android.kotlin.lockscreen.lock_screen.channel_description" // todo change real des.
    private const val CHANNEL_ID = "com.flow.android.kotlin.lockscreen.lock_screen.channel_id"
}