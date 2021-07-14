package com.flow.android.kotlin.lockscreen.preference.display.view

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.databinding.PreferenceBinding
import com.flow.android.kotlin.lockscreen.preferences.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisplayConfigurationFragment : ConfigurationFragment() {
    private val duration = 300L
    private val fontSizes by lazy { resources.getIntArray(R.array.font_size) }

    private val oldFontSize by lazy { Preference.getFontSize(requireContext()) }
    private var newFontSize = -1F

    override val toolbarTitleResId: Int = R.string.configuration_activity_001

    override fun onPause() {
        if (newFontSize != oldFontSize)
            viewModel.fondSizeChanged = true

        super.onPause()
    }

    override fun createConfigurationAdapter(): PreferenceAdapter {
        val context = requireContext()

        return PreferenceAdapter(arrayListOf(
                AdapterItem.SwitchItem(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_dark_mode_24),
                        isChecked = Preference.getIsNightMode(context),
                        onCheckedChange = { isChecked ->
                            lifecycleScope.launch {
                                val darkMode = if (isChecked)
                                    AppCompatDelegate.MODE_NIGHT_YES
                                else
                                    AppCompatDelegate.MODE_NIGHT_NO

                                withContext(Dispatchers.IO) {
                                    Preference.putIsNightMode(requireContext(), isChecked)
                                    delay(duration)
                                }

                                AppCompatDelegate.setDefaultNightMode(darkMode)
                            }
                        },
                        title = getString(R.string.display_configuration_fragment_000)
                ),
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_format_size_24),
                        description = "${oldFontSize.toInt()}sp",
                        onClick = { viewBinding: PreferenceBinding, _ ->
                            MaterialAlertDialogBuilder(requireContext()).setItems(fontSizes.map { "${it}sp" }.toTypedArray()) { _, i ->
                                val text = "${fontSizes[i]}sp"

                                Preference.putFontSize(requireContext(), fontSizes[i].toFloat())
                                viewBinding.textViewSummary.text = text
                                newFontSize = fontSizes[i].toFloat()
                            }.show()
                        },
                        title = getString(R.string.display_configuration_fragment_001)
                ),
        ))
    }
}