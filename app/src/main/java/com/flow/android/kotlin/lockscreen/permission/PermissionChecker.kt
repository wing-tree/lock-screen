package com.flow.android.kotlin.lockscreen.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.flow.android.kotlin.lockscreen.application.MainApplication

internal object PermissionChecker {
    internal fun checkPermission(context: Context, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else
            true
    }

    internal fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            permissions.forEach {
                with(checkPermission(context, it)) {
                    if (this.not())
                        return false
                }
            }
        }

        return true
    }

    internal fun hasCalendarPermission(): Boolean {
        return checkPermissions(
            MainApplication.instance,
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        )
    }

    internal fun hasManageOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true

        return Settings.canDrawOverlays(MainApplication.instance)
    }
}