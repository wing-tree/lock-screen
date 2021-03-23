package com.flow.android.kotlin.lockscreen.lock_screen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.main.view.MainActivity

class LockScreenService : JobIntentService() {

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    val newIntent = Intent(context, MainActivity::class.java)
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(newIntent)
                }
                else -> {
                }
            }
        }
    }

    override fun onHandleWork(intent: Intent) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)
        intentFilter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(broadcastReceiver, intentFilter)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            notificationChannel.description = CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_lock_open_24)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content_text))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notification: Notification = builder.build()

        startForeground(1, notification)
    }

    fun enqueueWork(context: Context?, work: Intent?) {
        context ?: return
        work ?: return

        enqueueWork(context, LockScreenService::class.java, JOB_ID, work)
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    @Suppress("SpellCheckingInspection")
    companion object {
        const val JOB_ID = 2251
        private const val CHANNEL_NAME = "com.flow.android.kotlin.lockscreen.lock_screen.channel_name"
        private const val CHANNEL_DESCRIPTION = "com.flow.android.kotlin.lockscreen.lock_screen.channel_description" // todo change real des.
        private const val CHANNEL_ID = "com.flow.android.kotlin.lockscreen.lock_screen.channel_id"
    }
}