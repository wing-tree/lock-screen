package com.flow.android.kotlin.lockscreen.notification.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.notification.service.NotificationListener

class NotificationBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        val localBroadcastManager = LocalBroadcastManager.getInstance(context)

        when(intent.action) {
            NotificationListener.Action.NOTIFICATION_POSTED -> {
                val sbn = intent.getParcelableExtra<StatusBarNotification>(
                        NotificationListener.Extra.NOTIFICATION_POSTED
                ) ?: return

                localBroadcastManager.sendBroadcast(Intent(intent.action).apply {
                    putExtra(NotificationListener.Extra.NOTIFICATION_POSTED, sbn)
                })
            }
            NotificationListener.Action.NOTIFICATION_REMOVED -> {
                val sbn = intent.getParcelableExtra<StatusBarNotification>(
                        NotificationListener.Extra.NOTIFICATION_REMOVED
                ) ?: return

                localBroadcastManager.sendBroadcast(Intent(intent.action).apply {
                    putExtra(NotificationListener.Extra.NOTIFICATION_REMOVED, sbn)
                })
            }
        }
    }
}