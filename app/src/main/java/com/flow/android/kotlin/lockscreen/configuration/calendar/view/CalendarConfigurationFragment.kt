package com.flow.android.kotlin.lockscreen.configuration.calendar.view

import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.databinding.PreferenceScreenBinding
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.util.collapse
import com.flow.android.kotlin.lockscreen.util.expand
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CalendarConfigurationFragment: ConfigurationFragment() {
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())
    private val contentResolver by lazy { requireActivity().contentResolver }

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
                        onClick = { binding, item ->
                            if (item.isExpanded)
                                binding.recyclerView.expand(300L)
                            else
                                binding.recyclerView.collapse(300L)

                            item.isExpanded = item.isExpanded.not()
                        },
                        title = getString(R.string.calendar),
                )
        ))
    }
}