package com.flow.android.kotlin.lockscreen.calendar.view

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.lockscreen.calendar.adapter.EventsAdapter
import com.flow.android.kotlin.lockscreen.calendar.contract.CalendarContract
import com.flow.android.kotlin.lockscreen.calendar.viewmodel.CalendarViewModel
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment: BaseMainFragment<FragmentCalendarBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentCalendarBinding {
        return FragmentCalendarBinding.inflate(inflater, container, false)
    }

    private val viewModel by activityViewModels<CalendarViewModel>()

    private val duration = 150L

    private val activityResultLauncher = registerForActivityResult(CalendarContract()) { result ->
        lifecycleScope.launch {
            delay(duration)

            when (result) {
                CalendarLoader.RequestCode.EditEvent -> viewModel.callRefresh()
                CalendarLoader.RequestCode.InsertEvent -> viewModel.callRefresh()
            }
        }
    }

    private val eventsAdapter = EventsAdapter(arrayListOf()) {
        CalendarLoader.editEvent(activityResultLauncher, it)
    }

    private val itemCount = 7

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            val text =
                if (position == 0)
                    getString(R.string.today)
                else
                    Calendar.getInstance().apply {
                        add(Calendar.DATE, position)
                    }.timeInMillis.format()
            viewBinding.textViewDate.text = text
        }
    }

    private val simpleDateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.format_date_001), Locale.getDefault())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        initializeView()
        registerLifecycleObservers()
        setIconColor()
        setTextColor()

        return view
    }

    override fun onDestroyView() {
        viewBinding.viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.onDestroyView()
    }

    private fun registerLifecycleObservers() {
        viewModel.calendars.observe(viewLifecycleOwner, { calendarDisplays ->
            val uncheckedCalendarIds = Preference.Calendar.getUncheckedCalendarIds(requireContext())

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                for (i in 0..itemCount) {
                    CalendarLoader.calendarEvents(
                            viewModel.contentResolver,
                            calendarDisplays.filter {
                                uncheckedCalendarIds.contains(it.id.toString()).not()
                            }, i
                    ).also { events ->
                        withContext(Dispatchers.Main) {
                            events.let { eventsAdapter.add(it) }
                        }
                    }
                }
            }
        })

        viewModel.refresh.observe(viewLifecycleOwner, {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val uncheckedCalendarIds = Preference.Calendar.getUncheckedCalendarIds(requireContext())

                eventsAdapter.clear()

                for (i in 0..itemCount) {
                    CalendarLoader.calendarEvents(
                            viewModel.contentResolver,
                            viewModel.calendars.value?.filter {
                                uncheckedCalendarIds.contains(it.id.toString()).not()
                            } ?: emptyList(), i
                    ).also { events ->
                        events.let { eventsAdapter.add(it, false) }
                    }
                }

                withContext(Dispatchers.Main) {
                    eventsAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun initializeView() {
        viewBinding.appCompatImageView.setOnClickListener {
            CalendarLoader.insertEvent(activityResultLauncher)
        }

        viewBinding.viewPager2.adapter = eventsAdapter
        viewBinding.viewPager2.registerOnPageChangeCallback(onPageChangeCallback)

        viewBinding.imageViewBack.setOnClickListener {
            if (viewBinding.viewPager2.currentItem > 0)
                viewBinding.viewPager2.currentItem -= 1
        }

        viewBinding.imageViewForward.setOnClickListener {
            if (viewBinding.viewPager2.currentItem < itemCount)
                viewBinding.viewPager2.currentItem += 1
        }
    }

    private fun setIconColor() {
        viewBinding.imageViewBack.setColorFilter(mainViewModel.viewPagerRegionColor, PorterDuff.Mode.SRC_IN)
        viewBinding.imageViewForward.setColorFilter(mainViewModel.viewPagerRegionColor, PorterDuff.Mode.SRC_IN)
        viewBinding.appCompatImageView.setColorFilter(mainViewModel.viewPagerRegionColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setTextColor() {
        viewBinding.textViewDate.setTextColor(mainViewModel.viewPagerRegionColor)
    }

    private fun Long.format() = simpleDateFormat.format(this)
}