package com.flow.android.kotlin.lockscreen.base

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    private val activityResultLauncherMap = mutableMapOf<String, ActivityResultLauncher<Intent>>()

    protected fun putActivityResultLauncher(key: String, value: ActivityResultLauncher<Intent>) {
        activityResultLauncherMap[key] = value
    }

    protected fun getActivityResultLauncher(key: String) = activityResultLauncherMap[key]
}