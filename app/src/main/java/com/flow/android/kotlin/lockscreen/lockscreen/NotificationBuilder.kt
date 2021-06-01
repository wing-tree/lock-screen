package com.flow.android.kotlin.lockscreen.lockscreen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.main.view.MainActivity
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.repository.Repository
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

            // 여기서 텍스트 설정.. 선택된 객체 1개 리턴할 것.

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_round_lock_open_24)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.notification_content_text))
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_MIN)

            it.onSuccess(builder)
        }
    }

    private fun todayEvents(context: Context) {
        val contentResolver = context.contentResolver

        val calendars = CalendarHelper.calendarDisplays(contentResolver)
        val uncheckedCalendarIds = ConfigurationPreferences.getUncheckedCalendarIds(context)

        val events = CalendarHelper.events(context.contentResolver, calendars.filter {
            uncheckedCalendarIds.contains(it.id.toString()).not()
        }, 0)


    }

    private fun todayMemos(context: Context) {
        val aHourInMillis = 3600000L
        val tenMinutesInMillis = 600000L
        val currentTimeMillis = System.currentTimeMillis()

        val todayMemos = Repository(context).getTodayMemos()

        todayMemos.sortedWith(Comparator { o1, o2 ->
            return@Comparator when {
                o1.alarmTime - o2.alarmTime < 0 -> 1
                else -> -1
            }
        })

        for (memo in todayMemos) {
            if (memo.alarmTime - tenMinutesInMillis <= currentTimeMillis && currentTimeMillis <= memo.alarmTime + aHourInMillis)
                println("memo.")
        }
    }

    private const val CHANNEL_NAME = "com.flow.android.kotlin.lockscreen.lock_screen.channel_name"
    private const val CHANNEL_DESCRIPTION = "com.flow.android.kotlin.lockscreen.lock_screen.channel_description" // todo change real des.
    private const val CHANNEL_ID = "com.flow.android.kotlin.lockscreen.lock_screen.channel_id"

    private data class NotificationText(
            val title: String,
            val subText: String,
            val text: String
    )
}