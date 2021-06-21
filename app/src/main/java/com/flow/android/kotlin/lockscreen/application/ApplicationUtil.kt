package com.flow.android.kotlin.lockscreen.application

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.MediaStore
import com.flow.android.kotlin.lockscreen.util.BLANK
import timber.log.Timber

object ApplicationUtil {
    fun findCameraAppPackageName(context: Context): String? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = context.packageManager
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

        if (resolveInfoList.isEmpty())
            return null

        for (resolveInfo in resolveInfoList) {
            if (isSystemApp(context, resolveInfo.activityInfo.packageName))
                return resolveInfo.activityInfo.packageName
        }

        return resolveInfoList[0].activityInfo.packageName
    }

    private fun isSystemApp(context: Context, packageName: String): Boolean {
        val applicationInfo = try {
            context.packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)

            return false
        }

        val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP

        return applicationInfo.flags and mask != 0
    }

    fun getApplicationLabel(packageManager: PackageManager, packageName: String): String {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)

            return packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            BLANK
        }
    }
}