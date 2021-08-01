package com.flow.android.kotlin.lockscreen.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


internal object PermissionChecker {
    private const val PREFIX = "com.flow.android.kotlin.lockscreen.permission" +
            ".PermissionChecker"

    object Calendar {
        const val KEY = "$PREFIX.Calendar.KEY"
    }

    private var snackbar: Snackbar? = null

    internal fun checkPermission(context: Context, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else
            true
    }

    internal fun checkPermissions(context: Context, permissions: List<String>): Boolean {
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

    internal fun checkPermissions(
            context: Context,
            permissions: List<String>,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit,
            onPermissionChecked: (() -> Unit)? = null
    ) {
        Dexter.withContext(context)
                .withPermissions(permissions)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.grantedPermissionResponses.map { it.permissionName }.containsAll(permissions)) {
                            onPermissionGranted.invoke()
                        }

                        if (report.deniedPermissionResponses.map { it.permissionName }.containsAll(permissions)) {
                            onPermissionDenied.invoke()
                        }

                        onPermissionChecked?.invoke()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest>,
                            token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    internal fun hasCalendarPermission(): Boolean {
        return checkPermissions(
                MainApplication.instance,
                listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        )
    }

    internal fun hasManageOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true

        return Settings.canDrawOverlays(MainApplication.instance)
    }

    fun showRequestCalendarPermissionSnackbar(view: View, activityResultLauncher: ActivityResultLauncher<Intent>?) {
        val context = view.context

        snackbar?.dismiss()

        snackbar = Snackbar.make(view, context.getString(R.string.permission_checker_001), Snackbar.LENGTH_INDEFINITE)
                .setAction(context.getString(R.string.permission_checker_000)) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", it.context.packageName, null)

                    activityResultLauncher?.launch(intent.apply { data = uri })
                }
                .setActionTextColor(ContextCompat.getColor(context, R.color.text))

        snackbar?.show()
    }

    fun dismissSnackbar(): Boolean {
        if (snackbar?.isShownOrQueued == true) {
            snackbar?.dismiss()
            return true
        }

        return false
    }
}