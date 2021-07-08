package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.calendar.model.CalendarModel
import com.flow.android.kotlin.lockscreen.calendar.model.CalendarEventModel
import com.flow.android.kotlin.lockscreen.configuration.viewmodel.ConfigurationChange
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut
import com.flow.android.kotlin.lockscreen.repository.ShortcutRepository
import com.flow.android.kotlin.lockscreen.shortcut.model.*
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contentResolver = application.contentResolver
    private val shortcutRepository = ShortcutRepository(application)

    override fun onCleared() {
        shortcutRepository.clearCompositeDisposable()
        super.onCleared()
    }

    private var _viewPagerRegionColor = Color.WHITE
    val viewPagerRegionColor: Int
        @ColorInt
        get() = _viewPagerRegionColor

    private val _calendars = MutableLiveData<List<CalendarModel>>()
    val calendars: LiveData<List<CalendarModel>>
        get() = _calendars

    fun calendarDisplays() = calendars.value
    fun contentResolver(): ContentResolver = contentResolver

    private val _events = MutableLiveData<List<CalendarEventModel>>()

    private val _refreshEvents = SingleLiveEvent<Unit>()
    val refreshEvents: SingleLiveEvent<Unit>
        get() = _refreshEvents

    fun callRefreshEvents() {
        _refreshEvents.call()
    }

    private val _refreshMemos = SingleLiveEvent<ConfigurationChange>()
    val refreshMemos: SingleLiveEvent<ConfigurationChange>
        get() = _refreshMemos

    private fun callRefreshMemos(configurationChange: ConfigurationChange? = null) {
        configurationChange?.let {
            _refreshMemos.value = it
        } ?: run {
            _refreshMemos.call()
        }
    }

    fun refresh(configurationChange: ConfigurationChange) {
        if (configurationChange.calendarChanged)
            callRefreshEvents()

        if (configurationChange.fondSizeChanged)
            callRefreshMemos()
    }

    fun submitEvents(calendarEvents: List<CalendarEventModel>) {
        _events.postValue(calendarEvents)
    }

    fun postCalendarDisplays() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _calendars.postValue(CalendarLoader.calendarDisplays(contentResolver))
            }
        }
    }

    fun postEvents() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _events.postValue(CalendarLoader.events(contentResolver, CalendarLoader.calendarDisplays(contentResolver), 0))
            }
        }
    }

    fun refreshCalendarDisplays() {
        val value = calendars.value ?: return
        _calendars.value = value
    }
}