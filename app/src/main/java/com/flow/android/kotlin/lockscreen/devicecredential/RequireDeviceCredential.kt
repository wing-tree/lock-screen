package com.flow.android.kotlin.lockscreen.devicecredential

import android.content.Context

interface RequireDeviceCredential {
    fun confirmDeviceCredential(context: Context)
}