package com.flow.android.kotlin.lockscreen.application

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.BuildConfig
import com.flow.android.kotlin.lockscreen.bottomnavigation.BottomNavigationItemPressedListener
import com.flow.android.kotlin.lockscreen.bottomnavigation.NavigationBarWatcher
import com.flow.android.kotlin.lockscreen.lockscreen.service.LockScreenService
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainApplication: Application(), Application.ActivityLifecycleCallbacks {
    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        AppCompatDelegate.setDefaultNightMode(Preference.Display.getDarkMode(this))
        registerActivityLifecycleCallbacks(this)

        if (BuildConfig.DEBUG)
            Timber.plant(DebugTree())
    }

    private val homeWatcher = NavigationBarWatcher(this)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        with(homeWatcher) {
            setOnNavigationBarItemPressedListener(homePressedListener())
            startWatch()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        homeWatcher.stopWatch()
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    private fun homePressedListener() = object : BottomNavigationItemPressedListener {
        override fun onHomeKeyPressed() {
            localBroadcastManager.sendBroadcastSync(Intent(LockScreenService.Action.HomeKeyPressed))
        }

        override fun onRecentAppsPressed() {
            localBroadcastManager.sendBroadcastSync(Intent(LockScreenService.Action.RecentAppsPressed))
        }
    }

    companion object {
        private lateinit var INSTANCE: MainApplication
        val instance
            get() = INSTANCE
    }
}