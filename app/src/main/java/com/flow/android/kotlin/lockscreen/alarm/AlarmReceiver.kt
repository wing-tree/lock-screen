package com.flow.android.kotlin.lockscreen.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

//class AlarmReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent?) {
//        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//        val manager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationIntent = Intent(context, EditActivity::class.java)
//        val serviceIntent = Intent(context, AlarmIntentService::class.java)
//        val id = intent!!.getIntExtra(KEY_NOTE_ID, DEFAULT_VALUE_NOTE_ID)
//        if (id == DEFAULT_VALUE_NOTE_ID) return
//        val title = intent.getStringExtra(KEY_NOTE_TITLE)
//        val content = intent.getStringExtra(KEY_NOTE_CONTENT)
//
//        notificationIntent.action = ACTION_ALARM_NOTIFICATION_CLICKED + id
//        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
//                Intent.FLAG_ACTIVITY_SINGLE_TOP
//        notificationIntent.putExtra(KEY_NOTE_ID, id)
//
//        val pendingIntent = PendingIntent.getActivity(
//                context,
//                id,
//                notificationIntent,
//                PendingIntent.FLAG_ONE_SHOT
//        )
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
//
//            builder.setSmallIcon(R.drawable.ic_cat_00_orange_32dp)
//            channel.description = CHANNEL_DESCRIPTION
//            manager.createNotificationChannel(channel)
//        } else builder.setSmallIcon(R.mipmap.ic_cat_00_orange_128px)
//
//        builder.setAutoCancel(true)
//                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
//                .setContentTitle(title)
//                .setContentText(content)
//                .setContentInfo(CONTENT_INFO)
//                .setContentIntent(pendingIntent)
//        manager.notify(id, builder.build())
//
//        serviceIntent.putExtra(KEY_NOTE_ID, id)
//        context.startService(serviceIntent)
//    }
//}