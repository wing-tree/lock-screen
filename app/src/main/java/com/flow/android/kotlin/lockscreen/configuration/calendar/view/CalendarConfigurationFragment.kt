package com.flow.android.kotlin.lockscreen.configuration.calendar.view

import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.databinding.PreferenceBinding
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.util.collapse
import com.flow.android.kotlin.lockscreen.util.expand
import com.flow.android.kotlin.lockscreen.util.measuredHeight
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CalendarConfigurationFragment: ConfigurationFragment() {
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())
    private val contentResolver by lazy { requireActivity().contentResolver }
    private val duration = 300L

    private val uncheckedCalendarIds = arrayListOf<String>()

    override fun onPause() {
        if (uncheckedCalendarIds != ConfigurationPreferences.getUncheckedCalendarIds(requireContext()).toList())
            viewModel.calendarChanged = true

        super.onPause()
    }

    override val toolbarTitleResId: Int = R.string.configuration_activity_005

    override fun createConfigurationAdapter(): ConfigurationAdapter {
        val context = requireContext()
        uncheckedCalendarIds.addAll(ConfigurationPreferences.getUncheckedCalendarIds(context))
        val calendarDisplays = CalendarLoader.calendars(contentResolver).map {
            CheckBoxItem(
                    isChecked = uncheckedCalendarIds.contains(it.id.toString()).not(),
                    text = it.name,
                    onCheckedChange = { isChecked ->
                        if (isChecked)
                            ConfigurationPreferences.removeUncheckedCalendarId(context, it.id.toString())
                        else
                            ConfigurationPreferences.addUncheckedCalendarId(context, it.id.toString())
                    }
            )
        }

        checkBoxAdapter.addAll(calendarDisplays)

        return ConfigurationAdapter(arrayListOf(
                AdapterItem.ListItem(
                        adapter = checkBoxAdapter,
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_today_24),
                        onClick = { viewBinding, item ->
                            if (item.isExpanded) {
                                val constrainedHeight = resources.getDimensionPixelSize(R.dimen.double_extra_large_200)
                                val heightOneLine = resources.getDimensionPixelSize(R.dimen.height_one_line)

                                var to = checkBoxAdapter.itemCount * heightOneLine

                                if (to > constrainedHeight)
                                    to = constrainedHeight

                                viewBinding.recyclerView.expand(duration, to)
                            } else
                                viewBinding.recyclerView.collapse(duration, 0)

                            item.isExpanded = item.isExpanded.not()
                        },
                        title = getString(R.string.calendar),
                ),
            AdapterItem.Item(
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_format_size_24),
                description = "sp",
                onClick = { viewBinding: PreferenceBinding, _ ->

                },
                title = getString(R.string.display_configuration_fragment_001)
            )
        ))
    }
}