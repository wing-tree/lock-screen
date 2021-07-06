package com.flow.android.kotlin.lockscreen.notification.service

import android.content.Intent
import android.os.*
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.*

class NotificationListener: NotificationListenerService() {
    private val binder = NotificationListenerBinder()

    object Action {
        const val NOTIFICATION_POSTED = "com.flow.android.kotlin.lockscreen.notification" +
                ".NotificationListener.Action.NOTIFICATION_POSTED"
        const val NOTIFICATION_REMOVED = "com.flow.android.kotlin.lockscreen.notification" +
                ".NotificationListener.Action.NOTIFICATION_REMOVED"
    }

    object Extra {
        const val NOTIFICATION_POSTED = "com.flow.android.kotlin.lockscreen.notification" +
                ".NotificationListener.Extra.NOTIFICATIONS_POSTED"
        const val NOTIFICATION_REMOVED = "com.flow.android.kotlin.lockscreen.notification" +
                ".NotificationListener.Extra.NOTIFICATION_REMOVED"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { sendBroadcast(Intent(Action.NOTIFICATION_POSTED).apply {
            putExtra(Extra.NOTIFICATION_POSTED, it)
        }) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { sendBroadcast(Intent(Action.NOTIFICATION_REMOVED).apply {
            putExtra(Extra.NOTIFICATION_REMOVED, it)
        }) }
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