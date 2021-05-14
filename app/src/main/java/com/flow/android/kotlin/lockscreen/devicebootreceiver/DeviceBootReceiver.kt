package com.flow.android.kotlin.lockscreen.devicebootreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.flow.android.kotlin.lockscreen.lockscreen.LockScreenService
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences

class DeviceBootReceiver : BroadcastReceiver()  {
    override fun onReceive(context: Context, intent: Intent) {
        if (ConfigurationPreferences.getShowOnLockScreen(context))
            startService(context)
    }

    private fun startService(context: Context) {
        val intent = Intent(context, LockScreenService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent)
        else
            context.startService(intent)
    }
}