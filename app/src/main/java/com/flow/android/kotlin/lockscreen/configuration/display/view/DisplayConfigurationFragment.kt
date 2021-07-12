package com.flow.android.kotlin.lockscreen.configuration.display.view

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.databinding.PreferenceBinding
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisplayConfigurationFragment : ConfigurationFragment() {
    private val duration = 300L
    private val fontSizes by lazy { resources.getIntArray(R.array.font_size) }

    private val oldFontSize by lazy { ConfigurationPreferences.getFontSize(requireContext()) }
    private var newFontSize = -1F

    override val toolbarTitleResId: Int = R.string.configuration_activity_001

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
                        onClick = { viewBinding: PreferenceBinding, _ ->
                            MaterialAlertDialogBuilder(requireContext()).setItems(fontSizes.map { "${it}sp" }.toTypedArray()) { _, i ->
                                val text = "${fontSizes[i]}sp"

                                ConfigurationPreferences.putFontSize(requireContext(), fontSizes[i].toFloat())
                                viewBinding.textViewSummary.text = text
                                newFontSize = fontSizes[i].toFloat()
                            }.show()
                        },
                        title = getString(R.string.display_configuration_fragment_001)
                ),
        ))
    }
}