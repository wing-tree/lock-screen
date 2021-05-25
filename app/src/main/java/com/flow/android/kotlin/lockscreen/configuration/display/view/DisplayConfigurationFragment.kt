package com.flow.android.kotlin.lockscreen.configuration.display.view

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.databinding.ItemBinding
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisplayConfigurationFragment : ConfigurationFragment() {
    override fun createConfigurationAdapter(): ConfigurationAdapter {
        val context = requireContext()

        return ConfigurationAdapter(arrayListOf(
                AdapterItem.SwitchItem(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_dark_mode_24),
                        isChecked = ConfigurationPreferences.getDarkMode(context),
                        onCheckedChange = { isChecked ->
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    ConfigurationPreferences.putDarkMode(requireContext(), isChecked)
                                    delay(duration)
                                }

                                AppCompatDelegate.setDefaultNightMode(
                                        if (isChecked)
                                            AppCompatDelegate.MODE_NIGHT_YES
                                        else
                                            AppCompatDelegate.MODE_NIGHT_NO
                                )
                            }
                        },
                        title = getString(R.string.display_configuration_fragment_000)
                ),
                AdapterItem.Item(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_format_color_text_24),
                        description = lockScreenTextColors[ConfigurationPreferences.getLockScreenTextColor(requireContext())],
                        onClick = { itemBinding: ItemBinding, _ ->
                            MaterialAlertDialogBuilder(requireContext()).setItems(lockScreenTextColors) { _, i ->
                                when (i) {
                                    0 -> {
                                        ConfigurationPreferences.putLockScreenTextColor(requireContext(), LockScreenTextColor.Black)
                                        itemBinding.textDescription.text = lockScreenTextColors[0]
                                    }
                                    1 -> {
                                        ConfigurationPreferences.putLockScreenTextColor(requireContext(), LockScreenTextColor.White)
                                        itemBinding.textDescription.text = lockScreenTextColors[1]
                                    }
                                    2 -> {
                                        ConfigurationPreferences.putLockScreenTextColor(requireContext(), LockScreenTextColor.DependingOnWallpaper)
                                        itemBinding.textDescription.text = lockScreenTextColors[2]
                                    }
                                }
                            }.show()
                        },
                        title = getString(R.string.display_configuration_fragment_001)
                ),
        ))
    }

    private val duration = 300L

    object LockScreenTextColor {
        const val Black = 0
        const val White = 1
        const val DependingOnWallpaper = 2
    }

    private val lockScreenTextColors by lazy {
        requireContext().resources.getStringArray(R.array.display_configuration_fragment_002)
    }
}