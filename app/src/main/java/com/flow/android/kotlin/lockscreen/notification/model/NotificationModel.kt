package com.flow.android.kotlin.lockscreen.notification.model

import android.app.Notification

data class NotificationModel(
        val label: String,
        val notification: Notification,
        val postTime: Long
)