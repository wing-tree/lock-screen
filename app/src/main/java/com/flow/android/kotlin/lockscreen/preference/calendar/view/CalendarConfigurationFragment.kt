package com.flow.android.kotlin.lockscreen.preference.calendar.view

import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.preference.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.preferences.Preference
import com.flow.android.kotlin.lockscreen.util.collapse
import com.flow.android.kotlin.lockscreen.util.expand

class CalendarConfigurationFragment: ConfigurationFragment() {
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())
    private val contentResolver by lazy { requireActivity().contentResolver }
    private val duration = 300L

    private val uncheckedCalendarIds = arrayListOf<String>()

    override fun onPause() {
        if (uncheckedCalendarIds != Preference.getUncheckedCalendarIds(requireContext()).toList())
            viewModel.calendarChanged = true

        super.onPause()
    }

    override val toolbarTitleResId: Int = R.string.configuration_activity_005

    override fun createConfigurationAdapter(): PreferenceAdapter {
        val context = requireContext()
        uncheckedCalendarIds.addAll(Preference.getUncheckedCalendarIds(context))
        val calendarDisplays = CalendarLoader.calendars(contentResolver).map {
            CheckBoxItem(
                    isChecked = uncheckedCalendarIds.contains(it.id.toString()).not(),
                    text = it.name,
                    onCheckedChange = { isChecked ->
                        if (isChecked)
                            Preference.removeUncheckedCalendarId(context, it.id.toString())
                        else
                            Preference.addUncheckedCalendarId(context, it.id.toString())
                    }
            )
        }

        checkBoxAdapter.addAll(calendarDisplays)

        return PreferenceAdapter(arrayListOf(
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

                                viewBinding.constraintLayoutEntries.expand(duration, to.inc())
                            } else
                                viewBinding.constraintLayoutEntries.collapse(duration, 0)

                            item.isExpanded = item.isExpanded.not()
                        },
                        title = getString(R.string.calendar),
                )
        ))
    }
}