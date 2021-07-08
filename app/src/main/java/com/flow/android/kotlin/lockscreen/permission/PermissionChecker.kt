package com.flow.android.kotlin.lockscreen.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

internal object PermissionChecker {
    @RequiresApi(Build.VERSION_CODES.M)
    internal fun checkPermission(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}