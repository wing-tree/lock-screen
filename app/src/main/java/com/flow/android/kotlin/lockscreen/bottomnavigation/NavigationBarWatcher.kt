package com.flow.android.kotlin.lockscreen.bottomnavigation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import timber.log.Timber
import java.lang.Exception

class NavigationBarWatcher(private val context: Context) {
    private val intentFilter: IntentFilter by lazy { IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) }

    private var bottomNavigationItemPressedListener: BottomNavigationItemPressedListener? = null
    private var innerBroadcastReceiver: InnerBroadcastReceiver? = null

    fun setOnNavigationBarItemPressedListener(bottomNavigationItemPressedListener: BottomNavigationItemPressedListener) {
        this.bottomNavigationItemPressedListener = bottomNavigationItemPressedListener
    }

    fun startWatch() {
        bottomNavigationItemPressedListener?.let {
            innerBroadcastReceiver = InnerBroadcastReceiver()
            context.registerReceiver(innerBroadcastReceiver, intentFilter)
        }
    }

    fun stopWatch() {
        bottomNavigationItemPressedListener?.let {
            innerBroadcastReceiver?.let {
                context.unregisterReceiver(it)
                innerBroadcastReceiver = null
            }
        }
    }

    internal inner class InnerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action

            if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val systemDialogReasonKey = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY) ?: return

                bottomNavigationItemPressedListener?.let {
                    when(systemDialogReasonKey) {
                        SYSTEM_DIALOG_REASON_HOME_KEY -> it.onHomeKeyPressed()
                        SYSTEM_DIALOG_REASON_RECENT_APPS -> it.onRecentAppsPressed()
                    }
                }
            }
        }
    }

    companion object {
        private const val SYSTEM_DIALOG_REASON_KEY = "reason"
        @Suppress("SpellCheckingInspection")
        private const val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"
        @Suppress("SpellCheckingInspection")
        private const val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"
    }
}