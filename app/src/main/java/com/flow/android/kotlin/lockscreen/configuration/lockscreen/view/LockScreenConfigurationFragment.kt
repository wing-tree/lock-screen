package com.flow.android.kotlin.lockscreen.configuration.lockscreen.view

import android.content.Intent
import android.os.Build
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.configuration.view.ConfigurationActivity
import com.flow.android.kotlin.lockscreen.lockscreen.service.LockScreenService
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences

class LockScreenConfigurationFragment : ConfigurationFragment() {
    private object Id {
        const val DisplayAfterUnlocking = 2249L
    }

    override val toolbarTitleResId: Int = R.string.configuration_activity_003

    override fun createConfigurationAdapter(): ConfigurationAdapter {
        val context = requireContext()

        return ConfigurationAdapter(arrayListOf(
                AdapterItem.SwitchItem(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_lock_24),
                        isChecked = ConfigurationPreferences.getShowOnLockScreen(context),
                        onCheckedChange = { isChecked ->
                            ConfigurationPreferences.putShowOnLockScreen(context, isChecked)

                            if (isChecked) {
                                configurationAdapter.showItem(Id.DisplayAfterUnlocking)

                                val intent = Intent(context, LockScreenService::class.java)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    context.startForegroundService(intent)
                                else
                                    context.startService(intent)
                            } else {
                                configurationAdapter.hideItem(Id.DisplayAfterUnlocking)
                                context.sendBroadcast(Intent(LockScreenService.Action.StopSelf))
                            }
                        },
                        title = getString(R.string.show_on_lock_screen)
                ),
                AdapterItem.SwitchItem(
                        drawable = null,
                        id = Id.DisplayAfterUnlocking,
                        isChecked = ConfigurationPreferences.getDisplayAfterUnlocking(context),
                        onCheckedChange = { isChecked ->
                            ConfigurationPreferences.putDisplayAfterUnlocking(context, isChecked)
                        },
                        title = getString(R.string.display_after_unlocking)
                )
        ))
    }
}