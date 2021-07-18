package com.flow.android.kotlin.lockscreen.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.main.view.MainActivity

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
        return MainApplication.instance?.let {
            checkPermissions(
                    it,
                    arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
            )
        } ?: false
    }

    internal fun hasManageOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true

        MainApplication.instance?.let {
            return Settings.canDrawOverlays(it)
        } ?: return false
    }
}