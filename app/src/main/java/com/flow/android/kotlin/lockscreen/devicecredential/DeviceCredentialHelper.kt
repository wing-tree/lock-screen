package com.flow.android.kotlin.lockscreen.devicecredential

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.provider.Settings


object DeviceCredentialHelper {
    fun confirmDeviceCredential(context: Context) {
        val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val biometricPrompt = BiometricPrompt.Builder(context)
                        .setTitle("title")
                        .setNegativeButton("negative", {

                        }, { i, j ->

                        }).build()

                biometricPrompt.allowedAuthenticators
            } else {
                @Suppress("DEPRECATION")
                val biometricPrompt = BiometricPrompt.Builder(context)
                        .setTitle("title")
                        .setDeviceCredentialAllowed(true)
                        .build()

                biometricPrompt.authenticate(CancellationSignal(), context.mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                    }

                    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                        super.onAuthenticationHelp(helpCode, helpString)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                })
            }
        } else {
            @Suppress("DEPRECATION")
            val intent = keyguardManager.createConfirmDeviceCredentialIntent("title", "description")

            context.startActivity(intent)
        }
    }

    fun isDeviceScreenLocked(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            isDeviceLocked(context)
        else
            isPatternSet(context) || isPassOrPinSet(context)
    }

    private fun isPatternSet(context: Context): Boolean {
        return try {
            @Suppress("DEPRECATION")
            val lockPatternEnable: Int = Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.LOCK_PATTERN_ENABLED
            )

            lockPatternEnable == 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    private fun isPassOrPinSet(context: Context): Boolean {
        val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager //api 16+
        return keyguardManager.isKeyguardSecure
    }

    @TargetApi(23)
    private fun isDeviceLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager //api 23+
        return keyguardManager.isDeviceSecure
    }
}