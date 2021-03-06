package com.flow.android.kotlin.lockscreen.preference.lockscreen.view

import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.PreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference

class LockScreenPreferenceFragment : PreferenceFragment() {
    private object Id {
        const val ShowAfterUnlocking = 2249L
    }

    override val toolbarTitleResId: Int = R.string.lock_screen_preference_fragment_001

    override fun createPreferenceAdapter(): PreferenceAdapter {
        val context = requireContext()

        return PreferenceAdapter(arrayListOf(
                AdapterItem.SwitchPreference(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_unlock_90px),
                        isChecked = Preference.LockScreen.getShowOnLockScreen(context),
                        onCheckedChange = { isChecked ->
                            Preference.LockScreen.putShowOnLockScreen(context, isChecked)

                            val adapterItem = preferenceAdapter.getItem(Id.ShowAfterUnlocking)
                            val position = preferenceAdapter.getPosition(Id.ShowAfterUnlocking)

                            if (isChecked) {
                                adapterItem?.isClickable = true
                                adapterItem?.isVisible = true

                                if (position != -1)
                                    preferenceAdapter.notifyItemChanged(position)
                            } else {
                                adapterItem?.isClickable = false
                                adapterItem?.isVisible = false

                                if (position != -1)
                                    preferenceAdapter.notifyItemChanged(position)
                            }
                        },
                        title = getString(R.string.lock_screen_preference_fragment_002)
                ),
                AdapterItem.SwitchPreference(
                        drawable = null,
                        id = Id.ShowAfterUnlocking,
                        isChecked = Preference.LockScreen.getShowAfterUnlocking(context),
                        isClickable = Preference.LockScreen.getShowOnLockScreen(context),
                        isVisible = Preference.LockScreen.getShowOnLockScreen(context),
                        onCheckedChange = { isChecked ->
                            Preference.LockScreen.putShowAfterUnlocking(context, isChecked)
                        },
                        title = getString(R.string.lock_screen_preference_fragment_000)
                ),
                AdapterItem.SwitchPreference(
                        drawable = null,
                        isChecked = Preference.LockScreen.getUnlockWithBackKey(context),
                        onCheckedChange = { isChecked ->
                            Preference.LockScreen.putUnlockWithBackKey(context, isChecked)
                        },
                        title = getString(R.string.lock_screen_preference_fragment_003)
                )
        ))
    }
}