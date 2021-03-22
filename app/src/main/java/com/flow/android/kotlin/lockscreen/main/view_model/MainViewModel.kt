package com.flow.android.kotlin.lockscreen.main.view_model

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.calendar.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel: ViewModel() {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = _events

    fun events(contentResolver: ContentResolver) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _events.postValue(CalendarHelper.events(contentResolver, CalendarHelper.calendarDisplays(contentResolver)))
            }
        }
    }
}