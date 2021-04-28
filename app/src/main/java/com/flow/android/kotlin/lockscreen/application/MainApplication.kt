package com.flow.android.kotlin.lockscreen.application

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import com.flow.android.kotlin.lockscreen.BuildConfig
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainApplication: Application(), Application.ActivityLifecycleCallbacks {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(DebugTree())
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        // 확인 필요. todo.
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}