package com.flow.android.kotlin.lockscreen.home.homewatcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class HomeWatcher(private val context: Context) {
    private val intentFilter: IntentFilter by lazy { IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) }
    private val innerReceiver: InnerReceiver by lazy { InnerReceiver() }
    private var homePressedListener: HomePressedListener? = null

    fun setOnHomePressedListener(homePressedListener: HomePressedListener) {
        this.homePressedListener = homePressedListener
    }

    fun startWatch() {
        homePressedListener?.let {
            context.registerReceiver(innerReceiver, intentFilter)
        }
    }

    fun stopWatch() {
        homePressedListener?.let {
            context.unregisterReceiver(innerReceiver)
        }
    }

    internal inner class InnerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action

            if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val systemDialogReasonKey = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY) ?: return

                homePressedListener?.let {
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