package com.flow.android.kotlin.lockscreen.application

import android.app.Application
import com.flow.android.kotlin.lockscreen.BuildConfig
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(DebugTree())
    }
}