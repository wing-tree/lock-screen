package com.flow.android.kotlin.lockscreen.main.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.main.view.MainActivity

object ManageOverlayPermissionNotificationBuilder {
    const val ID = 2328519

    private const val PREFIX = "com.flow.android.kotlin.lockscreen.main.notification.NotificationBuilder"
    private const val CHANNEL_ID = "$PREFIX.CHANNEL_ID"
    private const val CHANNEL_NAME = "$PREFIX.CHANNEL_NAME"
    private const val CHANNEL_DESCRIPTION = "com.flow.android.kotlin.lockscreen.lock_screen.channel_description" // todo change real des.

    fun create(context: Context): NotificationCompat.Builder {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)

            notificationChannel.description = CHANNEL_DESCRIPTION
            notificationChannel.setShowBadge(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val uri = Uri.fromParts("package", context.packageName, null)

        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
        else
            Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentTitle = context.getString(R.string.manage_overlay_permission_notification_builder_000)
        val contentText = context.getString(R.string.manage_overlay_permission_notification_builder_001)
        val color = ContextCompat.getColor(context, R.color.deep_orange_400)

        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mobile_48px)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setColor(color)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setShowWhen(true)
    }
}