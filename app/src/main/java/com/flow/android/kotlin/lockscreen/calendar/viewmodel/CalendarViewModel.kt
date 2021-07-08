package com.flow.android.kotlin.lockscreen.calendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flow.android.kotlin.lockscreen.calendar.model.CalendarModel
import com.flow.android.kotlin.lockscreen.calendar.model.CalendarEventModel
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent

class CalendarViewModel(application: Application): AndroidViewModel(application) {
    private val contentResolver = application.contentResolver

    private val _calendars = MutableLiveData<List<CalendarModel>>()
    val calendars: LiveData<List<CalendarModel>>
        get() = _calendars

    private val _events = MutableLiveData<List<CalendarEventModel>>()

    private val _refreshEvents = SingleLiveEvent<Unit>()
    val refreshEvents: SingleLiveEvent<Unit>
        get() = _refreshEvents
}