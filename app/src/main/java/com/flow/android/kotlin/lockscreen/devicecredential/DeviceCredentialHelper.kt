package com.flow.android.kotlin.lockscreen.devicecredential

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.util.BLANK
import timber.log.Timber

object DeviceCredentialHelper {
    object RequestCode {
        const val ConfirmDeviceCredential = 1309
    }

    fun requireUnlock(context: Context): Boolean {
        val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            return keyguardManager.isDeviceLocked

        return keyguardManager.isKeyguardLocked
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun confirmDeviceCredential(activity: Activity, keyguardDismissCallback: KeyguardManager.KeyguardDismissCallback) {
        val keyguardManager = activity.getSystemService(KeyguardManager::class.java)

        keyguardManager.requestDismissKeyguard(activity, keyguardDismissCallback)
    }

    fun confirmDeviceCredential(fragment: Fragment) {
        val keyguardManager = fragment.requireActivity().getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val title = if (lockPatternEnable(fragment.requireContext()))
            fragment.getString(R.string.device_credential_helper_000)
        else
            fragment.getString(R.string.device_credential_helper_001)

        val intent = keyguardManager.createConfirmDeviceCredentialIntent(title, BLANK)

        fragment.startActivityForResult(intent, RequestCode.ConfirmDeviceCredential)
    }

    private fun lockPatternEnable(context: Context): Boolean {
        return try {
            @Suppress("DEPRECATION")
            val lockPatternEnable: Int = Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.LOCK_PATTERN_ENABLED
            )

            lockPatternEnable == 1
        } catch (e: Settings.SettingNotFoundException) {
            Timber.e(e)
            false
        }
    }
}