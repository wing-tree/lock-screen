package com.flow.android.kotlin.lockscreen.notification.model

import android.app.Notification
import androidx.viewbinding.ViewBinding

data class NotificationModel(
        var expanded: Boolean = false,
        val group: String,
        val groupKey: String,
        val id: Long,
        val isGroup: Boolean,
        val label: String,
        val notification: Notification,
        val packageName: String,
        val postTime: Long,
        val template: String,
        val children: ArrayList<NotificationModel>,
        var childHeight: Int = 0
)