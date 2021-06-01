package com.flow.android.kotlin.lockscreen.configuration.calendar.view

import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences

class CalendarConfigurationFragment: ConfigurationFragment() {
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())

    override fun createConfigurationAdapter(): ConfigurationAdapter {
        val context = requireContext()
        val uncheckedCalendarIds = ConfigurationPreferences.getUncheckedCalendarIds(context)
        val calendarDisplays = CalendarHelper.calendarDisplays(viewModel.contentResolver()).map {
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
                        onClick = { listItemBinding, listItem ->

                        },
                        title = getString(R.string.calendar),
                )
        ))
    }
}