package com.flow.android.kotlin.lockscreen.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.lockscreen.main.adapter.EventAdapter
import com.flow.android.kotlin.lockscreen.main.view_model.MainViewModel
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences

class CalendarFragment: Fragment(), EventAdapter.OnItemClickListener {
    private val eventAdapter = EventAdapter(this)
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
        initializeAdapter()
        initializeLiveData()

        return viewBinding?.root
    }

    private fun initializeAdapter() {
        viewBinding?.recyclerView?.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun initializeLiveData() {
        viewModel.calendarDisplays.observe(viewLifecycleOwner, { calendarDisplays ->
            val uncheckedCalendarIds = ConfigurationPreferences.getUncheckedCalendarIds(requireContext())

            CalendarHelper.events(viewModel.contentResolver(), calendarDisplays.filter {
                uncheckedCalendarIds.contains(it.id.toString()).not()
            }).also { events ->
                eventAdapter.submitList(events)
            }
        })
    }

    private fun initializeView() {
        viewBinding?.appCompatImageView?.setOnClickListener {
            CalendarHelper.insertEvent(requireActivity())
        }
    }

    /** EventAdapter.OnItemClickListener */
    override fun onItemClick(item: Event) {
        CalendarHelper.editEvent(requireActivity(), item)
    }
}