package com.flow.android.kotlin.lockscreen.preference.lockscreen.view

import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.lockscreen.service.LockScreenService
import com.flow.android.kotlin.lockscreen.preferences.Preference

class LockScreenConfigurationFragment : ConfigurationFragment() {
    private object Id {
        const val DisplayAfterUnlocking = 2249L
    }

    override val toolbarTitleResId: Int = R.string.configuration_activity_003

    override fun createConfigurationAdapter(): PreferenceAdapter {
        val context = requireContext()

        return PreferenceAdapter(arrayListOf(
                AdapterItem.SwitchItem(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_lock_24),
                        isChecked = Preference.getShowOnLockScreen(context),
                        onCheckedChange = { isChecked ->
                            Preference.putShowOnLockScreen(context, isChecked)

                            if (isChecked) {
                                preferenceAdapter.showItem(Id.DisplayAfterUnlocking)

                                val intent = Intent(context, LockScreenService::class.java)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    context.startForegroundService(intent)
                                else
                                    context.startService(intent)
                            } else {
                                preferenceAdapter.hideItem(Id.DisplayAfterUnlocking)
                                context.sendBroadcast(Intent(LockScreenService.Action.StopSelf))
                            }
                        },
                        title = getString(R.string.show_on_lock_screen)
                ),
                AdapterItem.SwitchItem(
                        drawable = null,
                        id = Id.DisplayAfterUnlocking,
                        isChecked = Preference.getShowAfterUnlocking(context),
                        onCheckedChange = { isChecked ->
                            Preference.putShowAfterUnlocking(context, isChecked)
                        },
                        title = getString(R.string.display_after_unlocking)
                )
        ))
    }
}