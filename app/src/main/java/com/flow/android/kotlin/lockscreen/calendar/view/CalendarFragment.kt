package com.flow.android.kotlin.lockscreen.calendar.view

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.lockscreen.calendar.adapter.EventsAdapter
import com.flow.android.kotlin.lockscreen.calendar.contract.CalendarContract
import com.flow.android.kotlin.lockscreen.calendar.model.Model
import com.flow.android.kotlin.lockscreen.calendar.viewmodel.CalendarViewModel
import com.flow.android.kotlin.lockscreen.permission.PermissionChecker
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.util.hide
import com.flow.android.kotlin.lockscreen.util.show
import io.reactivex.disposables.CompositeDisposable
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

    private val viewModel by viewModels<CalendarViewModel>()
    private val compositeDisposable = CompositeDisposable()
    private val duration = 150L

    private val activityResultLauncher = registerForActivityResult(CalendarContract()) { result ->
        lifecycleScope.launch {
            delay(duration)

            when (result) {
                CalendarLoader.RequestCode.EditEvent -> refresh(false)
                CalendarLoader.RequestCode.InsertEvent -> refresh(false)
            }
        }
    }

    private val adapter = EventsAdapter {
        currentItem = viewBinding.viewPager2.currentItem
        CalendarLoader.edit(activityResultLauncher, it)
    }
    private val itemCount = 7

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            val text =
                if (position == 0)
                    getString(R.string.calendar_fragment_000)
                else
                    Calendar.getInstance().apply {
                        add(Calendar.DATE, position)
                    }.timeInMillis.format()

            viewBinding.textViewDate.text = text

            when (position) {
                0 -> {
                    viewBinding.imageViewBack.isEnabled = false
                    viewBinding.imageViewForward.isEnabled = true
                }
                adapter.itemCount.dec() -> {
                    viewBinding.imageViewBack.isEnabled = true
                    viewBinding.imageViewForward.isEnabled = false
                }
                else -> {
                    viewBinding.imageViewBack.isEnabled = true
                    viewBinding.imageViewForward.isEnabled = true
                }
            }
        }
    }

    private val simpleDateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.format_date_001), Locale.getDefault())
    }

    private var currentItem = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        initializeViews()
        initializeData()
        registerLifecycleObservers()
        setTextColor()

        return view
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        viewBinding.viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.onDestroyView()
    }

    private fun registerLifecycleObservers() {
        mainViewModel.postCalendars.observe(viewLifecycleOwner, {
            viewModel.postValue()
        })

        viewModel.calendars.observe(viewLifecycleOwner, { calendars ->
            if (calendars.isEmpty())
                return@observe

            val uncheckedCalendarIds = Preference.Calendar.getUncheckedCalendarIds(requireContext())

            enableCalendarControlViews()

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val currentList = arrayListOf<List<Model.Event>>()

                for (i in 0..itemCount) {
                    CalendarLoader.events(
                            mainViewModel.contentResolver,
                            calendars.filter {
                                uncheckedCalendarIds.contains(it.id.toString()).not()
                            }, i
                    ).also { events ->
                        withContext(Dispatchers.Main) {
                            events.let { currentList.add(it) }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    adapter.submitList(currentList)
                }
            }
        })

        mainViewModel.refresh.observe(viewLifecycleOwner, {
            if (Preference.isChanged(it.fondSize, it.uncheckedCalendarIds))
                refresh()
        })
    }

    private fun initializeViews() {
        viewBinding.imageView.setOnClickListener {
            currentItem = viewBinding.viewPager2.currentItem
            CalendarLoader.insert(activityResultLauncher)
        }

        viewBinding.viewPager2.adapter = adapter
        viewBinding.viewPager2.registerOnPageChangeCallback(onPageChangeCallback)

        viewBinding.imageViewBack.setOnClickListener {
            if (viewBinding.viewPager2.currentItem > 0)
                viewBinding.viewPager2.currentItem -= 1
        }

        viewBinding.imageViewForward.setOnClickListener {
            if (viewBinding.viewPager2.currentItem < itemCount)
                viewBinding.viewPager2.currentItem += 1
        }

        viewBinding.noCalendarPermissionMessage.root.setOnClickListener {
            PermissionChecker.checkPermissions(requireContext(), listOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
            ), {
                viewModel.postValue()
            }, {
                disableCalendarControlViews()
                mainViewModel.callShowRequestCalendarPermissionSnackbar()
            })
        }
    }

    private fun initializeData() {
        if (PermissionChecker.hasCalendarPermission())
            viewModel.postValue()
        else
            disableCalendarControlViews()
    }

    private fun refresh(preferenceChanged: Boolean = true) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val currentList = adapter.currentList.toMutableList()
            val uncheckedCalendarIds = Preference.Calendar.getUncheckedCalendarIds(requireContext())

            for (i in 0..itemCount) {
                CalendarLoader.events(
                    mainViewModel.contentResolver,
                    viewModel.calendars.value?.filter {
                        uncheckedCalendarIds.contains(it.id.toString()).not()
                    } ?: emptyList(), i
                ).also { currentList[i] = it }
            }

            withContext(Dispatchers.Main) {
                adapter.submitList(currentList)

                if (preferenceChanged)
                    adapter.notifyDataSetChanged()

                if (adapter.itemCount > currentItem)
                    viewBinding.viewPager2.currentItem = currentItem
            }
        }
    }

    private fun enableCalendarControlViews() {
        viewBinding.imageView.isEnabled = true
        viewBinding.imageViewBack.isEnabled = true
        viewBinding.imageViewForward.isEnabled = true
        viewBinding.noCalendarPermissionMessage.root.hide(invisible = true)
        viewBinding.textViewDate.show()
    }

    private fun disableCalendarControlViews() {
        viewBinding.imageView.isEnabled = false
        viewBinding.imageViewBack.isEnabled = false
        viewBinding.imageViewForward.isEnabled = false
        viewBinding.noCalendarPermissionMessage.root.show()
        viewBinding.textViewDate.hide(invisible = true)
    }

    private fun setTextColor() {
        viewBinding.textViewDate.setTextColor(mainViewModel.viewPagerRegionColor)
    }

    private fun Long.format() = simpleDateFormat.format(this)
}