package com.flow.android.kotlin.lockscreen.devicecredential

interface RequireDeviceCredential<T> {
    fun confirmDeviceCredential(value: T)
}