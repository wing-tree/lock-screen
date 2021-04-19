package com.flow.android.kotlin.lockscreen.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.flow.android.kotlin.lockscreen.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.lockscreen.calendar.adapter.EventsAdapter
import com.flow.android.kotlin.lockscreen.main.view_model.MainViewModel
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarFragment: Fragment() {
    private val eventsAdapter = EventsAdapter(arrayListOf()) {
        CalendarHelper.editEvent(requireActivity(), it)
    }
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                MainViewModel(requireActivity().application) as T
        }).get(MainViewModel::class.java)
    }

    private var viewBinding: FragmentCalendarBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentCalendarBinding.inflate(inflater, container, false)

        initializeView()
        initializeLiveData()

        return viewBinding?.root
    }

    private fun initializeLiveData() {
        viewModel.calendarDisplays.observe(viewLifecycleOwner, { calendarDisplays ->
            val uncheckedCalendarIds = ConfigurationPreferences.getUncheckedCalendarIds(requireContext())

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                for (i in 0..7) {
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
        viewBinding?.appCompatImageView?.setOnClickListener {
            CalendarHelper.insertEvent(requireActivity())
        }

        viewBinding?.viewPager2?.adapter = eventsAdapter

        viewBinding?.imageViewForward?.setOnClickListener {
            viewBinding?.viewPager2?.currentItem = 2
        }
    }
}