package com.flow.android.kotlin.lockscreen.notification.broadcastreceiver

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.notification.service.NotificationListener

class NotificationBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        val localBroadcastManager = LocalBroadcastManager.getInstance(context)

        when(intent.action) {
            NotificationListener.Action.NOTIFICATION_POSTED -> {
                val activeNotifications = intent.getParcelableArrayListExtra<Notification>(
                        NotificationListener.Extra.ACTIVE_NOTIFICATIONS
                )

                if (activeNotifications.isNullOrEmpty())
                    return

                localBroadcastManager.sendBroadcast(Intent(intent.action).apply {
                    putParcelableArrayListExtra(NotificationListener.Extra.ACTIVE_NOTIFICATIONS, activeNotifications)
                })
            }
            NotificationListener.Action.NOTIFICATION_REMOVED -> {
                val notification = intent.getParcelableExtra<Notification>(
                        NotificationListener.Extra.NOTIFICATION_REMOVED
                ) ?: return

                localBroadcastManager.sendBroadcast(Intent(intent.action).apply {
                    putExtra(NotificationListener.Extra.NOTIFICATION_REMOVED, notification)
                })
            }
        }
    }
}