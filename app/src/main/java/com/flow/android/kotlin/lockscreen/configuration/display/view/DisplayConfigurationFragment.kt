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
    private val duration = 300L
    private val fontSizes by lazy { resources.getIntArray(R.array.font_size) }

    private val oldFontSize by lazy { ConfigurationPreferences.getFontSize(requireContext()) }
    private var newFontSize = -1F

    override fun onPause() {
        if (newFontSize != oldFontSize)
            viewModel.fondSizeChanged = true

        super.onPause()
    }

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
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_format_size_24),
                        description = "${oldFontSize.toInt()}sp",
                        onClick = { itemBinding: ItemBinding, _ ->
                            MaterialAlertDialogBuilder(requireContext()).setItems(fontSizes.map { "${it}sp" }.toTypedArray()) { _, i ->
                                val text = "${fontSizes[i]}sp"

                                ConfigurationPreferences.putFontSize(requireContext(), fontSizes[i].toFloat())
                                itemBinding.textDescription.text = text
                                newFontSize = fontSizes[i].toFloat()
                            }.show()
                        },
                        title = getString(R.string.display_configuration_fragment_001)
                ),
        ))
    }
}