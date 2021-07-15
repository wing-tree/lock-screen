package com.flow.android.kotlin.lockscreen.preference.lockscreen.view

import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.PreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.lockscreen.service.LockScreenService
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference

class LockScreenPreferenceFragment : PreferenceFragment() {
    private object Id {
        const val ShowAfterUnlocking = 2249L
    }

    override val toolbarTitleResId: Int = R.string.configuration_activity_003

    override fun createPreferenceAdapter(): PreferenceAdapter {
        val context = requireContext()

        return PreferenceAdapter(arrayListOf(
                AdapterItem.SwitchPreference(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_lock_24),
                        isChecked = Preference.LockScreen.getShowOnLockScreen(context),
                        onCheckedChange = { isChecked ->
                            Preference.LockScreen.putShowOnLockScreen(context, isChecked)

                            val adapterItem = preferenceAdapter.getItem(Id.ShowAfterUnlocking)
                            val position = preferenceAdapter.getPosition(Id.ShowAfterUnlocking)

                            if (isChecked) {
                                adapterItem?.isEnabled = true
                                adapterItem?.isVisible = true

                                if (position != -1)
                                    preferenceAdapter.notifyItemChanged(position)

                                val intent = Intent(context, LockScreenService::class.java)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    context.startForegroundService(intent)
                                else
                                    context.startService(intent)
                            } else {
                                adapterItem?.isEnabled = false
                                adapterItem?.isVisible = false

                                if (position != -1)
                                    preferenceAdapter.notifyItemChanged(position)

                                context.sendBroadcast(Intent(LockScreenService.Action.StopSelf))
                            }
                        },
                        title = getString(R.string.show_on_lock_screen)
                ),
                AdapterItem.SwitchPreference(
                        drawable = null,
                        id = Id.ShowAfterUnlocking,
                        isChecked = Preference.LockScreen.getShowAfterUnlocking(context),
                        isEnabled = Preference.LockScreen.getShowOnLockScreen(context),
                        isVisible = Preference.LockScreen.getShowOnLockScreen(context),
                        onCheckedChange = { isChecked ->
                            Preference.LockScreen.putShowAfterUnlocking(context, isChecked)
                        },
                        title = getString(R.string.display_after_unlocking)
                )
        ))
    }
}