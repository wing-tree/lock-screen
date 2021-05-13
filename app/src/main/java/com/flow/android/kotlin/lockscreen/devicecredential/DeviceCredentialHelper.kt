package com.flow.android.kotlin.lockscreen.devicecredential

import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment


object DeviceCredentialHelper {
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

        val intent = keyguardManager.createConfirmDeviceCredentialIntent("asdfdsfa", "sasdfdsf")

        fragment.startActivityForResult(intent, 1)
    }
}