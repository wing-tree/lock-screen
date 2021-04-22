package com.flow.android.kotlin.lockscreen.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.lockscreen.calendar.adapter.EventsAdapter
import com.flow.android.kotlin.lockscreen.main.view_model.MainViewModel
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment: Fragment() {
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

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                MainViewModel(requireActivity().application) as T
        }).get(MainViewModel::class.java)
    }

    private var _viewBinding: FragmentCalendarBinding? = null
    private val viewBinding: FragmentCalendarBinding
        get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentCalendarBinding.inflate(inflater, container, false)

        initializeView()
        initializeLiveData()

        return viewBinding.root
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

    private fun Long.format() = simpleDateFormat.format(this)
}