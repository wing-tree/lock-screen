package com.flow.android.kotlin.lockscreen.preference.display.view

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.PreferenceFragment
import com.flow.android.kotlin.lockscreen.base.SingleStringChoiceDialogFragment
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.databinding.PreferenceBinding
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.preference.view.SingleFontSizeChoiceDialogFragment
import com.flow.android.kotlin.lockscreen.util.BLANK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DisplayPreferenceFragment : PreferenceFragment() {
    private val date = Date()
    private val duration = 300L
    private val fontSizes = arrayOf(12F, 14F, 16F, 20F, 24F, 32F)
    private val timeFormats by lazy {
        arrayOf(
                getString(R.string.display_preference_fragment_002),
                format(getString(R.string.format_date_001), date),
                format(getString(R.string.format_date_002), date)
        )
    }

    override val toolbarTitleResId: Int = R.string.preference_activity_003

    private fun format(pattern: String, date: Date) =
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)

    override fun createPreferenceAdapter(): PreferenceAdapter {
        val context = requireContext()
        var timeFormat = Preference.Display.getTimeFormat(requireContext())

        if (timeFormat.isBlank())
            timeFormat = getString(R.string.display_preference_fragment_002)

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
                        title = getString(R.string.display_preference_fragment_000)
                ),
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_format_size_24),
                        summary = "${Preference.Display.getFontSize(requireContext())}dp",
                        onClick = { viewBinding: PreferenceBinding, _ ->
                            SingleFontSizeChoiceDialogFragment(fontSizes) { dialogFragment, item ->
                                val text = "${item}dp"

                                Preference.Display.putFontSize(requireContext(), item)

                                viewBinding.textViewSummary.text = text

                                dialogFragment.dismiss()
                            }.also { it.show(childFragmentManager, it.tag) }
                        },
                        title = getString(R.string.display_preference_fragment_001)
                ),
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_access_time_24),
                        summary = format(timeFormat, date),
                        onClick = { viewBinding: PreferenceBinding, _ ->
                            SingleStringChoiceDialogFragment(timeFormats) { dialogFragment, timeFormat ->
                                if (timeFormat == getString(R.string.display_preference_fragment_002))
                                    Preference.Display.putTimeFormat(requireContext(), BLANK)
                                else
                                    Preference.Display.putTimeFormat(requireContext(), timeFormat)

                                viewBinding.textViewSummary.text = timeFormat

                                dialogFragment.dismiss()
                            }.also { it.show(childFragmentManager, it.tag) }
                        },
                        title = getString(R.string.display_preference_fragment_003)
                )
        ))
    }
}