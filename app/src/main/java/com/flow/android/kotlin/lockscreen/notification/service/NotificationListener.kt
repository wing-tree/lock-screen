package com.flow.android.kotlin.lockscreen.notification.service

import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.flow.android.kotlin.lockscreen.notification.broadcastreceiver.NotificationBroadcastReceiver
import timber.log.Timber
import java.util.*

class NotificationListener: NotificationListenerService() {
    private val binder = NotificationListenerBinder()
    private var isConnected = false

    object Action {
        const val NOTIFICATION_POSTED = "com.flow.android.kotlin.lockscreen.notification" +
                ".NotificationListener.Action.NOTIFICATION_POSTED"
        const val NOTIFICATION_REMOVED = "com.flow.android.kotlin.lockscreen.notification" +
                ".NotificationListener.Action.NOTIFICATION_REMOVED"
    }

    object Extra {
        const val ACTIVE_NOTIFICATIONS = "com.flow.android.kotlin.lockscreen.notification" +
                ".NotificationListener.Extra.ACTIVE_NOTIFICATIONS"
        const val NOTIFICATION_REMOVED = "com.flow.android.kotlin.lockscreen.notification" +
                ".NotificationListener.Extra.NOTIFICATION_REMOVED"
    }

    private val  notificationBroadcastReceiver = NotificationBroadcastReceiver()

    override fun onListenerConnected() {
        super.onListenerConnected()
        registerReceiver(notificationBroadcastReceiver, IntentFilter().apply {
            addAction(Action.NOTIFICATION_POSTED)
            addAction(Action.NOTIFICATION_REMOVED)
        })

        isConnected = true
    }

    override fun onListenerDisconnected() {
        try {
            if (isConnected)
                unregisterReceiver(notificationBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        } finally {
            isConnected = false
        }

        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val activeNotifications = ArrayList(activeNotifications.map { it.notification })

        activeNotifications.add(notification)

//        sendBroadcast(Intent(Action.NOTIFICATION_POSTED).apply {
//                    putParcelableArrayListExtra(Extra.ACTIVE_NOTIFICATIONS, activeNotifications)
//                })
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return

        sendBroadcast(Intent(Action.NOTIFICATION_REMOVED).apply {
            putExtra(Extra.NOTIFICATION_REMOVED, notification)
        })
    }

    inner class NotificationListenerBinder : Binder() {
        fun getNotificationListener(): NotificationListener = this@NotificationListener
    }

    override fun onBind(intent: Intent): IBinder? {
        val action = intent.action
        return if (SERVICE_INTERFACE == action) {
            super.onBind(intent)
        } else {
            binder
        }
    }
}