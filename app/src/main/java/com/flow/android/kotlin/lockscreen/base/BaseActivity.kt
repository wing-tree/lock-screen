package com.flow.android.kotlin.lockscreen.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.lockscreen.service.LockScreenService

open class BaseActivity : AppCompatActivity() {
    private val activityResultLauncherMap = mutableMapOf<String, ActivityResultLauncher<Intent>>()
    private val bottomNavigationBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                LockScreenService.Action.HomeKeyPressed, LockScreenService.Action.RecentAppsPressed -> {
                    if (isFinishing.not())
                        finish()
                }
                else -> {
                    // pass
                }
            }
        }
    }

    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localBroadcastManager.registerReceiver(bottomNavigationBroadcastReceiver, IntentFilter().apply {
            addAction(LockScreenService.Action.HomeKeyPressed)
            addAction(LockScreenService.Action.RecentAppsPressed)
        })
    }

    override fun onDestroy() {
        localBroadcastManager.unregisterReceiver(bottomNavigationBroadcastReceiver)
        super.onDestroy()
    }

    protected fun putActivityResultLauncher(key: String, value: ActivityResultLauncher<Intent>) {
        activityResultLauncherMap[key] = value
    }

    protected fun getActivityResultLauncher(key: String) = activityResultLauncherMap[key]

    protected fun showToast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, text, duration).show()
    }
}