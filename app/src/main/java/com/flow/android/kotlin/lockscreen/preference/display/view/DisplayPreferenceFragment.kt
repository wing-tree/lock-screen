package com.flow.android.kotlin.lockscreen.preference.display.view

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.PreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.databinding.PreferenceBinding
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.preference.view.FontSizeSelectionListDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisplayPreferenceFragment : PreferenceFragment() {
    private val duration = 300L
    private val fontSizes = arrayOf(12F, 14F, 16F, 20F, 24F, 32F)

    private val oldFontSize by lazy { Preference.Display.getFontSize(requireContext()) }
    private var newFontSize = -1F

    override val toolbarTitleResId: Int = R.string.configuration_activity_001

    override fun onPause() {
        if (newFontSize != oldFontSize)
            viewModel.fondSizeChanged = true

        super.onPause()
    }

    override fun createPreferenceAdapter(): PreferenceAdapter {
        val context = requireContext()

        return PreferenceAdapter(arrayListOf(
                AdapterItem.SwitchPreference(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_dark_mode_24),
                        isChecked = Preference.Display.getIsDarkMode(context),
                        onCheckedChange = { isChecked ->
                            lifecycleScope.launch {
                                val darkMode = if (isChecked)
                                    AppCompatDelegate.MODE_NIGHT_YES
                                else
                                    AppCompatDelegate.MODE_NIGHT_NO

                                withContext(Dispatchers.IO) {
                                    Preference.Display.putIsDarkMode(requireContext(), isChecked)
                                    delay(duration)
                                }

                                AppCompatDelegate.setDefaultNightMode(darkMode)
                            }
                        },
                        title = getString(R.string.display_configuration_fragment_000)
                ),
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_format_size_24),
                        description = "${oldFontSize}dp",
                        onClick = { viewBinding: PreferenceBinding, _ ->
                            FontSizeSelectionListDialogFragment(fontSizes) { dialogFragment, item ->
                                val text = "${item}dp"

                                Preference.Display.putFontSize(requireContext(), item)

                                viewBinding.textViewSummary.text = text
                                newFontSize = item

                                dialogFragment.dismiss()
                            }.also {
                                it.show(childFragmentManager, it.tag)
                            }
                        },
                        title = getString(R.string.display_configuration_fragment_001)
                ),
        ))
    }
}