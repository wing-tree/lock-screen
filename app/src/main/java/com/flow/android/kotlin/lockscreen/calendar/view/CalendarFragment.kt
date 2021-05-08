package com.flow.android.kotlin.lockscreen.calendar.view

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseFragment
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.lockscreen.calendar.adapter.EventsAdapter
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment: BaseFragment<FragmentCalendarBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentCalendarBinding {
        return FragmentCalendarBinding.inflate(inflater, container, false)
    }

    private val eventsAdapter = EventsAdapter(arrayListOf()) {
        CalendarHelper.editEvent(requireActivity(), it)
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
        initializeLiveData()
        setIconColor()
        setTextColor()

        return view
    }

    override fun onDestroyView() {
        viewBinding.viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.onDestroyView()
    }

    private fun initializeLiveData() {
        viewModel.calendarDisplays.observe(viewLifecycleOwner, { calendarDisplays ->
            val uncheckedCalendarIds = ConfigurationPreferences.getUncheckedCalendarIds(requireContext())

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                for (i in 0..itemCount) {
                    CalendarHelper.events(
                            viewModel.contentResolver(),
                            calendarDisplays.filter {
                                uncheckedCalendarIds.contains(it.id.toString()).not()
                            }, i
                    ).also { events ->
                        withContext(Dispatchers.Main) {
                            events?.let { eventsAdapter.add(it) }
                        }
                    }
                }
            }
        })

        viewModel.colorDependingOnBackground.observe(viewLifecycleOwner, {
            viewBinding.appCompatImageView.setColorFilter(it.onViewPagerColor, PorterDuff.Mode.SRC_IN)
            viewBinding.imageViewBack.setColorFilter(it.onViewPagerColor, PorterDuff.Mode.SRC_IN)
            viewBinding.imageViewForward.setColorFilter(it.onViewPagerColor, PorterDuff.Mode.SRC_IN)
            viewBinding.textViewDate.setTextColor(it.onViewPagerColor)
        })
    }

    private fun initializeView() {
        viewBinding.appCompatImageView.setOnClickListener {
            CalendarHelper.insertEvent(requireActivity())
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
        viewBinding.imageViewBack.setColorFilter(viewModel.viewPagerRegionColor, PorterDuff.Mode.SRC_IN)
        viewBinding.imageViewForward.setColorFilter(viewModel.viewPagerRegionColor, PorterDuff.Mode.SRC_IN)
        viewBinding.appCompatImageView.setColorFilter(viewModel.viewPagerRegionColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setTextColor() {
        viewBinding.textViewDate.setTextColor(viewModel.viewPagerRegionColor)
    }

    private fun Long.format() = simpleDateFormat.format(this)
}