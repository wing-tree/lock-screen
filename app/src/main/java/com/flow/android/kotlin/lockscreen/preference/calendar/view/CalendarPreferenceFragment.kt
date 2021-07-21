package com.flow.android.kotlin.lockscreen.preference.calendar.view

import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.PreferenceFragment
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.preference.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.util.collapse
import com.flow.android.kotlin.lockscreen.util.expand

class CalendarPreferenceFragment: PreferenceFragment() {
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())
    private val contentResolver by lazy { requireActivity().contentResolver }
    private val duration = 300L

    private val uncheckedCalendarIds = arrayListOf<String>()

    override val toolbarTitleResId: Int = R.string.configuration_activity_005

    override fun createPreferenceAdapter(): PreferenceAdapter {
        val context = requireContext()
        uncheckedCalendarIds.addAll(Preference.Calendar.getUncheckedCalendarIds(context))
        val calendarDisplays = CalendarLoader.calendars(contentResolver).map {
            CheckBoxItem(
                    isChecked = uncheckedCalendarIds.contains(it.id.toString()).not(),
                    text = it.name,
                    onCheckedChange = { isChecked ->
                        if (isChecked)
                            Preference.Calendar.removeUncheckedCalendarId(context, it.id.toString())
                        else
                            Preference.Calendar.addUncheckedCalendarId(context, it.id.toString())
                    }
            )
        }

        checkBoxAdapter.addAll(calendarDisplays)

        return PreferenceAdapter(arrayListOf(
                AdapterItem.MultiSelectListPreference(
                        adapter = checkBoxAdapter,
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_calendar_today_24),
                        onClick = { viewBinding, item ->
                            if (item.isExpanded) {
                                val constrainedHeight = resources.getDimensionPixelSize(R.dimen.height_256dp)
                                val heightOneLine = resources.getDimensionPixelSize(R.dimen.height_48dp)

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